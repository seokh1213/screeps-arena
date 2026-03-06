package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Constraints
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.UnitMode
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.core.controlledById
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

/**
 * 단계형 보급 전술.
 *
 * 1) 초기 타워 충전
 * 2) 앵커 지점 합류
 * 3) 목표 플래그 주변 타워 충전
 *
 * Team 전용이 아니라, 어느 스쿼드든 anchorSquadId와 context만 바꿔 재사용 가능하다.
 */
class PhasedTowerSupportTactic(
    private val anchorSquadId: String,
    private val phaseStateKey: String = DEFAULT_PHASE_STATE_KEY,
    private val anchorPositionStateKey: String = RoleLayeredEngagementTactic.DEFAULT_ANCHOR_POSITION_STATE_KEY,
) : Tactic {
    override val name: String = "PhasedTowerSupport"

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        val workerCreep = members.firstOrNull() ?: return

        val targetFlag = tacticContext.targetFlagId?.let(world::getFlagById)
        val targetFlagPosition = targetFlag?.toPosition() ?: tacticContext.targetPos

        val anchorPosition = blackboard.getSquadValue<Pos>(anchorSquadId, anchorPositionStateKey)
            ?: targetFlagPosition
            ?: workerCreep.toPosition()

        val currentStageName = blackboard.getSquadValue<String>(squadId, phaseStateKey)
            ?: StageName.ChargeInitialTowers

        val initialTowers = tacticContext.targetTowerIds
            .mapNotNull(world::getTowerById)
            .ifEmpty { selectOwnedTowers(world) }

        val initialTowersCharged = initialTowers.isEmpty() || initialTowers.all { !it.needsEnergy() }
        val reachedAnchor = getRange(workerCreep, anchorPosition.toHasPosition()) <= 2

        val nextStage = determineNextStage(
            currentStage = currentStageName,
            initialTowersCharged = initialTowersCharged,
            reachedAnchor = reachedAnchor,
        )

        blackboard.setSquadValue(squadId, phaseStateKey, nextStage)

        val intent = when (nextStage) {
            StageName.ChargeInitialTowers -> {
                val targetTower = selectNearestNeedingEnergyTower(workerCreep, initialTowers)
                UnitIntent(
                    mode = UnitMode.Support,
                    desiredPos = targetTower?.toPosition() ?: anchorPosition,
                    focusTargetId = targetTower?.id?.toString(),
                    constraints = Constraints(retreatHpRatio = 0.3),
                )
            }

            StageName.JoinAnchor -> {
                UnitIntent(
                    mode = UnitMode.Advance,
                    desiredPos = anchorPosition,
                    constraints = Constraints(retreatHpRatio = 0.3),
                )
            }

            StageName.ChargeTargetAreaTowers -> {
                val targetAreaTowers = selectTargetAreaTowers(
                    world = world,
                    targetFlagId = targetFlag?.id?.toString(),
                    targetFlagPosition = targetFlagPosition,
                )
                val targetTower = selectNearestNeedingEnergyTower(workerCreep, targetAreaTowers)
                UnitIntent(
                    mode = UnitMode.Support,
                    desiredPos = targetTower?.toPosition() ?: anchorPosition,
                    focusTargetId = targetTower?.id?.toString(),
                    constraints = Constraints(retreatHpRatio = 0.3),
                )
            }

            else -> {
                UnitIntent(
                    mode = UnitMode.Support,
                    desiredPos = anchorPosition,
                    constraints = Constraints(retreatHpRatio = 0.3),
                )
            }
        }

        outputIntents[workerCreep.id.toString()] = intent
    }

    private fun determineNextStage(
        currentStage: String,
        initialTowersCharged: Boolean,
        reachedAnchor: Boolean,
    ): String {
        return when (currentStage) {
            StageName.ChargeInitialTowers -> {
                if (initialTowersCharged) StageName.JoinAnchor else StageName.ChargeInitialTowers
            }

            StageName.JoinAnchor -> {
                if (reachedAnchor) StageName.ChargeTargetAreaTowers else StageName.JoinAnchor
            }

            StageName.ChargeTargetAreaTowers -> StageName.ChargeTargetAreaTowers
            else -> StageName.ChargeInitialTowers
        }
    }

    private fun selectOwnedTowers(world: WorldModel): List<StructureTower> {
        return world.towers.filter { it.my == true }
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

        val flagPosition = targetFlagPosition ?: return emptyList()
        return world.towers
            .filter { getRange(it, flagPosition.toHasPosition()) <= 12 }
            .sortedBy { getRange(it, flagPosition.toHasPosition()) }
    }

    private fun selectNearestNeedingEnergyTower(workerCreep: Creep, towers: List<StructureTower>): StructureTower? {
        return towers
            .asSequence()
            .filter { it.needsEnergy() }
            .minByOrNull { getRange(workerCreep, it) }
    }

    object StageName {
        const val ChargeInitialTowers: String = "chargeInitialTowers"
        const val JoinAnchor: String = "joinAnchor"
        const val ChargeTargetAreaTowers: String = "chargeTargetAreaTowers"
    }

    companion object {
        const val DEFAULT_PHASE_STATE_KEY: String = "phasedTowerSupportStage"
    }
}

private fun StructureTower.needsEnergy(): Boolean =
    (store.getFreeCapacity(RESOURCE_ENERGY.toString()) ?: 0) > 0

private fun screeps.bindings.arena.Flag.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun StructureTower.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun Creep.toPosition(): Pos = Pos(x.toInt(), y.toInt())
