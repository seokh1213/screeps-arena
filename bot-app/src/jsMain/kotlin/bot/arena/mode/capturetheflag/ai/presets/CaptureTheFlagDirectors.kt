package bot.arena.mode.capturetheflag.ai.presets

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.core.controlledById
import bot.arena.mode.capturetheflag.ai.htn.AllocateSquadTask
import bot.arena.mode.capturetheflag.ai.htn.CompoundTask
import bot.arena.mode.capturetheflag.ai.htn.HierarchicalTaskNetworkTask
import bot.arena.mode.capturetheflag.ai.htn.Method
import bot.arena.mode.capturetheflag.ai.htn.SelectTwoNeutralFlagsTask
import bot.arena.mode.capturetheflag.ai.hybrid.CandidatePlan
import bot.arena.mode.capturetheflag.ai.hybrid.HybridPlanSelectorStrategyDirector
import bot.arena.mode.capturetheflag.ai.hybrid.InterruptRule
import bot.arena.mode.capturetheflag.ai.library.formations.LShapeFormation
import bot.arena.mode.capturetheflag.ai.library.formations.SimpleLineFormation
import bot.arena.mode.capturetheflag.ai.library.tactics.AdaptiveTowerSupportTactic
import bot.arena.mode.capturetheflag.ai.library.tactics.CombatLayout
import bot.arena.mode.capturetheflag.ai.library.tactics.PhasedTowerSupportTactic
import bot.arena.mode.capturetheflag.ai.library.tactics.RoleLayeredEngagementTactic
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector
import screeps.bindings.ATTACK
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

/**
 * CTF Director 프리셋.
 *
 * 조립 포인트:
 * - Utility Objective 목록
 * - Hierarchical Task Network Subtask 목록
 * - Hybrid A의 Candidate Plan 목록 + Squad Template 목록
 */
object CaptureTheFlagDirectors {
    private const val frontlineDelayCaptureSquadId = "frontlineDelayCaptureSquad"
    private const val supportChargeWorkerSquadId = "supportChargeWorkerSquad"
    private const val mainAssaultCombatSquadId = "mainAssaultCombatSquad"
    private const val mainAssaultWorkerSupportSquadId = "mainAssaultWorkerSupportSquad"

    fun hybridUtilitySelectingHierarchicalTaskNetworkPlans(): StrategyDirector {
        val candidatePlans = candidatePlanTemplates.map { candidateTemplate ->
            CandidatePlan(
                id = candidateTemplate.id,
                rootTask = buildHybridRootTask(candidateTemplate.operationMode, squadPlanTemplates),
                utilityScore = { world, blackboard, _ -> candidateTemplate.utilityScorer(world, blackboard) },
            )
        }

        val interruptRule: InterruptRule = { world, _, _, activePlanId ->
            activePlanId != "regroupHomeThenReengage" && isEmergencyState(world)
        }

        return HybridPlanSelectorStrategyDirector(
            candidates = candidatePlans,
            switchCost = 14.0,
            minLockTicks = 12,
            interrupt = interruptRule,
        )
    }

    private fun buildHybridRootTask(
        operationMode: StrategicOperationMode,
        squadTemplates: List<SquadPlanTemplate>,
    ): CompoundTask {
        return object : CompoundTask {
            override val name: String = "HybridRoot($operationMode)"

            override fun methods(world: WorldModel, blackboard: Blackboard): List<Method> {
                val subtasks: MutableList<HierarchicalTaskNetworkTask> = mutableListOf(
                    SelectTwoNeutralFlagsTask(),
                )

                squadTemplates.forEach { squadTemplate ->
                    val allocateTask = AllocateSquadTask(
                        squadId = squadTemplate.squadId,
                        requirement = squadTemplate.capabilityRequest,
                        tactic = squadTemplate.tacticFactory(),
                        context = { worldModel, blackboardState ->
                            squadTemplate.contextFactory(worldModel, blackboardState, operationMode)
                        },
                    )
                    subtasks += allocateTask
                }

                return listOf(
                    Method(
                        name = "ExecuteHybridPlan($operationMode)",
                        precondition = { _, _ -> true },
                        subtasks = subtasks,
                    ),
                )
            }
        }
    }

    private val candidatePlanTemplates: List<CandidatePlanTemplate> = listOf(
        CandidatePlanTemplate(
            id = "secureNeutralFlagsAndCharge",
            operationMode = StrategicOperationMode.CONTEST_NEUTRAL_FLAGS,
            utilityScorer = { world, blackboard -> scoreSecureNeutralFlagsAndCharge(world, blackboard) },
        ),
        CandidatePlanTemplate(
            id = "pushEnemyTerritoryWithSamePattern",
            operationMode = StrategicOperationMode.PUSH_ENEMY_TERRITORY,
            utilityScorer = { world, blackboard -> scorePushEnemyTerritory(world, blackboard) },
        ),
        CandidatePlanTemplate(
            id = "regroupHomeThenReengage",
            operationMode = StrategicOperationMode.REGROUP_AT_HOME,
            utilityScorer = { world, blackboard -> scoreRegroupAtHome(world, blackboard) },
        ),
    )

    private val squadPlanTemplates: List<SquadPlanTemplate> = listOf(
        SquadPlanTemplate(
            squadId = frontlineDelayCaptureSquadId,
            capabilityRequest = CapabilityRequest(
                minMelee = 1,
                minRanged = 1,
                minHeal = 1,
                maxSize = 3,
            ),
            tacticFactory = {
                RoleLayeredEngagementTactic(
                    combatLayout = CombatLayout.HookFrontline,
                    gateSquadId = supportChargeWorkerSquadId,
                    gateStateKey = PhasedTowerSupportTactic.DEFAULT_PHASE_STATE_KEY,
                    gateReadyValue = PhasedTowerSupportTactic.StageName.ChargeTargetAreaTowers,
                )
            },
            contextFactory = { world, blackboard, operationMode ->
                val frontlineTargetFlag = selectFrontlineTargetFlag(world, blackboard, operationMode)
                TacticContext(
                    targetFlagId = frontlineTargetFlag?.id?.toString(),
                    targetPos = frontlineTargetFlag?.toPosition(),
                    formation = LShapeFormation(),
                )
            },
        ),
        SquadPlanTemplate(
            squadId = supportChargeWorkerSquadId,
            capabilityRequest = CapabilityRequest(minWorker = 1, maxSize = 1),
            tacticFactory = { PhasedTowerSupportTactic(anchorSquadId = frontlineDelayCaptureSquadId) },
            contextFactory = { world, blackboard, operationMode ->
                val frontlineTargetFlag = selectFrontlineTargetFlag(world, blackboard, operationMode)
                val initialTowerIds = selectHomeBaseTowers(world).map { it.id.toString() }
                TacticContext(
                    targetFlagId = frontlineTargetFlag?.id?.toString(),
                    targetPos = frontlineTargetFlag?.toPosition(),
                    targetTowerIds = initialTowerIds,
                )
            },
        ),
        SquadPlanTemplate(
            squadId = mainAssaultCombatSquadId,
            capabilityRequest = CapabilityRequest(
                minMelee = 3,
                minRanged = 3,
                minHeal = 3,
                maxSize = 9,
            ),
            tacticFactory = {
                RoleLayeredEngagementTactic(
                    combatLayout = CombatLayout.ThreeLineAssault,
                    captureWhileEnemyDetected = true,
                )
            },
            contextFactory = { world, blackboard, operationMode ->
                val frontlineTargetFlag = selectFrontlineTargetFlag(world, blackboard, operationMode)
                val mainAssaultTargetFlag =
                    selectMainAssaultTargetFlag(world, blackboard, operationMode, frontlineTargetFlag)
                TacticContext(
                    targetFlagId = mainAssaultTargetFlag?.id?.toString(),
                    targetPos = mainAssaultTargetFlag?.toPosition(),
                    formation = SimpleLineFormation(width = 3),
                )
            },
        ),
        SquadPlanTemplate(
            squadId = mainAssaultWorkerSupportSquadId,
            capabilityRequest = CapabilityRequest(minWorker = 1, maxSize = 1),
            tacticFactory = { AdaptiveTowerSupportTactic() },
            contextFactory = { world, blackboard, operationMode ->
                val frontlineTargetFlag = selectFrontlineTargetFlag(world, blackboard, operationMode)
                val mainAssaultTargetFlag =
                    selectMainAssaultTargetFlag(world, blackboard, operationMode, frontlineTargetFlag)
                val mainAssaultTowers = selectTargetAreaTowers(
                    world,
                    mainAssaultTargetFlag?.id?.toString(),
                    mainAssaultTargetFlag?.toPosition()
                )
                TacticContext(
                    targetFlagId = mainAssaultTargetFlag?.id?.toString(),
                    targetPos = mainAssaultTargetFlag?.toPosition(),
                    targetTowerIds = mainAssaultTowers.map { it.id.toString() },
                    rallyPos = selectOwnedFlags(world).firstOrNull()?.toPosition(),
                )
            },
        ),
    )

    private fun scoreSecureNeutralFlagsAndCharge(world: WorldModel, blackboard: Blackboard): Double {
        val neutralFlagCount = world.flags.count { it.my == null }
        val emergencyPenalty = if (isEmergencyState(world)) 55.0 else 0.0
        val stabilizedPenalty = if (isPrimaryAnchorAreaStabilized(world, blackboard)) 24.0 else 0.0
        return 70.0 + neutralFlagCount * 12.0 - emergencyPenalty - stabilizedPenalty
    }

    private fun scorePushEnemyTerritory(world: WorldModel, blackboard: Blackboard): Double {
        val enemyFlagCount = world.flags.count { it.my == false }
        val anchorStabilizedBonus = if (isPrimaryAnchorAreaStabilized(world, blackboard)) 78.0 else -20.0
        val emergencyPenalty = if (isEmergencyState(world)) 42.0 else 0.0
        val combatAdvantage = (combatPower(world.myCreeps) - combatPower(world.enemyCreeps)).toDouble()

        return 16.0 +
                enemyFlagCount * 8.0 +
                anchorStabilizedBonus +
                combatAdvantage * 1.8 -
                emergencyPenalty
    }

    private fun scoreRegroupAtHome(world: WorldModel, blackboard: Blackboard): Double {
        val emergencyBonus = if (isEmergencyState(world)) 80.0 else 0.0
        val healthPressureBonus = (1.0 - averageHealthRatio(world.myCreeps)) * 35.0
        val enemyPressureBonus = (combatPower(world.enemyCreeps) - combatPower(world.myCreeps)).coerceAtLeast(0) * 2.8
        val ownedFlagBonus = if (selectOwnedFlags(world).isNotEmpty()) 8.0 else 0.0
        val unstableAnchorBonus = if (!isPrimaryAnchorAreaStabilized(world, blackboard)) 12.0 else 0.0

        return 10.0 +
                emergencyBonus +
                healthPressureBonus +
                enemyPressureBonus +
                ownedFlagBonus +
                unstableAnchorBonus
    }

    private fun isPrimaryAnchorAreaStabilized(world: WorldModel, blackboard: Blackboard): Boolean {
        val primaryAnchorFlag = selectFrontlineTargetFlag(
            world = world,
            blackboard = blackboard,
            operationMode = StrategicOperationMode.CONTEST_NEUTRAL_FLAGS,
        ) ?: return false

        if (primaryAnchorFlag.my != true) return false

        val nearbyEnemies = world.enemiesInRange(primaryAnchorFlag, 10).size
        val nearbyAllies = world.alliesInRange(primaryAnchorFlag, 10).size
        val criticalInjuryRatio = criticalHealthRatio(world.myCreeps)

        val towersNearAnchor = world.towers.filter { getRange(it, primaryAnchorFlag) <= 12 }
        val towersStillNeedingEnergy = towersNearAnchor.any {
            (it.store.getFreeCapacity(RESOURCE_ENERGY.toString()) ?: 0) > 0
        }

        return nearbyEnemies <= 1 &&
                nearbyAllies >= 3 &&
                criticalInjuryRatio < 0.3 &&
                !towersStillNeedingEnergy
    }

    private fun isEmergencyState(world: WorldModel): Boolean {
        if (world.myCreeps.isEmpty()) return false

        val criticalInjuryRatio = criticalHealthRatio(world.myCreeps)
        val enemyCombatPower = combatPower(world.enemyCreeps)
        val myCombatPower = combatPower(world.myCreeps)

        return criticalInjuryRatio >= 0.45 || enemyCombatPower > myCombatPower + 6
    }

    private fun selectFrontlineTargetFlag(
        world: WorldModel,
        blackboard: Blackboard,
        operationMode: StrategicOperationMode,
    ): Flag? {
        return when (operationMode) {
            StrategicOperationMode.CONTEST_NEUTRAL_FLAGS -> {
                selectPrimaryContestFlag(world, blackboard)
            }

            StrategicOperationMode.PUSH_ENEMY_TERRITORY -> {
                selectEnemyFlags(world).firstOrNull() ?: selectPrimaryContestFlag(world, blackboard)
            }

            StrategicOperationMode.REGROUP_AT_HOME -> {
                selectOwnedFlags(world).firstOrNull() ?: selectPrimaryContestFlag(world, blackboard)
            }
        }
    }

    private fun selectMainAssaultTargetFlag(
        world: WorldModel,
        blackboard: Blackboard,
        operationMode: StrategicOperationMode,
        frontlineTargetFlag: Flag?,
    ): Flag? {
        return when (operationMode) {
            StrategicOperationMode.CONTEST_NEUTRAL_FLAGS -> {
                selectSecondaryContestFlag(world, blackboard, excludedFlagId = frontlineTargetFlag?.id?.toString())
            }

            StrategicOperationMode.PUSH_ENEMY_TERRITORY -> {
                selectEnemyFlags(world)
                    .firstOrNull { it.id.toString() != frontlineTargetFlag?.id?.toString() }
                    ?: selectSecondaryContestFlag(
                        world,
                        blackboard,
                        excludedFlagId = frontlineTargetFlag?.id?.toString()
                    )
            }

            StrategicOperationMode.REGROUP_AT_HOME -> {
                val ownedFlags = selectOwnedFlags(world)
                ownedFlags.getOrNull(1)
                    ?: ownedFlags.firstOrNull()
                    ?: selectSecondaryContestFlag(
                        world,
                        blackboard,
                        excludedFlagId = frontlineTargetFlag?.id?.toString()
                    )
            }
        }
    }

    private fun selectPrimaryContestFlag(world: WorldModel, blackboard: Blackboard): Flag? {
        val storedPrimaryFlag = blackboard.primaryTargetFlagId?.let(world::getFlagById)
        if (storedPrimaryFlag != null) return storedPrimaryFlag

        val neutralContestFlag = selectNeutralFlags(world).firstOrNull()
        if (neutralContestFlag != null) {
            blackboard.primaryTargetFlagId = neutralContestFlag.id.toString()
            return neutralContestFlag
        }

        return selectOwnedFlags(world).firstOrNull()
            ?: selectEnemyFlags(world).firstOrNull()
    }

    private fun selectSecondaryContestFlag(
        world: WorldModel,
        blackboard: Blackboard,
        excludedFlagId: String?,
    ): Flag? {
        val storedSecondaryFlag = blackboard.secondaryTargetFlagId
            ?.let(world::getFlagById)
            ?.takeIf { it.id.toString() != excludedFlagId }
        if (storedSecondaryFlag != null) return storedSecondaryFlag

        val neutralAlternative = selectNeutralFlags(world)
            .firstOrNull { it.id.toString() != excludedFlagId }
        if (neutralAlternative != null) {
            blackboard.secondaryTargetFlagId = neutralAlternative.id.toString()
            return neutralAlternative
        }

        return blackboard.secondaryTargetFlagId?.let(world::getFlagById)
            ?: blackboard.primaryTargetFlagId?.let(world::getFlagById)
            ?: selectOwnedFlags(world).firstOrNull()
            ?: selectEnemyFlags(world).firstOrNull()
    }

    private fun selectNeutralFlags(world: WorldModel): List<Flag> =
        world.flags.filter { it.my == null }.sortedBy { it.x }

    private fun selectEnemyFlags(world: WorldModel): List<Flag> =
        world.flags.filter { it.my == false }.sortedBy { it.x }

    private fun selectOwnedFlags(world: WorldModel): List<Flag> =
        world.flags.filter { it.my == true }.sortedBy { it.x }

    private fun selectHomeBaseTowers(world: WorldModel): List<StructureTower> {
        val ownedFlagIds = world.flags
            .filter { it.my == true }
            .map { it.id.toString() }
            .toSet()

        if (ownedFlagIds.isEmpty()) return world.towers.filter { it.my == true }

        val controlledByOwnedFlags = world.towers.filter { it.my == true && it.controlledById in ownedFlagIds }
        return if (controlledByOwnedFlags.isNotEmpty()) controlledByOwnedFlags else world.towers.filter { it.my == true }
    }

    private fun selectTargetAreaTowers(
        world: WorldModel,
        targetFlagId: String?,
        targetFlagPosition: Pos?,
    ): List<StructureTower> {
        if (targetFlagId != null) {
            val controlledByTargetFlag = world.towers.filter { it.controlledById == targetFlagId }
            if (controlledByTargetFlag.isNotEmpty()) return controlledByTargetFlag
        }

        val position = targetFlagPosition ?: return emptyList()
        return world.towers
            .filter { getRange(it, position.toHasPosition()) <= 12 }
            .sortedBy { getRange(it, position.toHasPosition()) }
    }

    private fun towersControlledByFlag(world: WorldModel, flagId: String): List<StructureTower> =
        world.towers.filter { it.controlledById == flagId }

    private fun combatPower(creeps: List<Creep>): Int {
        return creeps.sumOf { creep ->
            creep.body.sumOf { bodyPart ->
                if (bodyPart.hits <= 0) return@sumOf 0
                when (bodyPart.type) {
                    ATTACK -> 3
                    RANGED_ATTACK -> 3
                    HEAL -> 2
                    else -> 0
                }
            }
        }
    }

    private fun averageHealthRatio(creeps: List<Creep>): Double {
        if (creeps.isEmpty()) return 0.0
        return creeps.map { it.hitPointRatio() }.average()
    }

    private fun criticalHealthRatio(creeps: List<Creep>): Double {
        if (creeps.isEmpty()) return 0.0
        val criticalCreepCount = creeps.count { it.hitPointRatio() <= 0.35 }
        return criticalCreepCount.toDouble() / creeps.size.toDouble()
    }

    private data class CandidatePlanTemplate(
        val id: String,
        val operationMode: StrategicOperationMode,
        val utilityScorer: (WorldModel, Blackboard) -> Double,
    )

    private data class SquadPlanTemplate(
        val squadId: String,
        val capabilityRequest: CapabilityRequest,
        val tacticFactory: () -> Tactic,
        val contextFactory: (WorldModel, Blackboard, StrategicOperationMode) -> TacticContext,
    )
}

private enum class StrategicOperationMode {
    CONTEST_NEUTRAL_FLAGS,
    PUSH_ENEMY_TERRITORY,
    REGROUP_AT_HOME,
}

private fun Flag.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun Creep.hitPointRatio(): Double =
    if (hitsMax == 0) 0.0 else hits.toDouble() / hitsMax.toDouble()
