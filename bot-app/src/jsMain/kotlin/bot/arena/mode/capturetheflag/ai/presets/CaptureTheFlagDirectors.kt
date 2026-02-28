package bot.arena.mode.capturetheflag.ai.presets

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.core.controlledById
import bot.arena.mode.capturetheflag.ai.htn.AllocateSquadTask
import bot.arena.mode.capturetheflag.ai.htn.CompoundTask
import bot.arena.mode.capturetheflag.ai.htn.HierarchicalTaskNetworkStrategyDirector
import bot.arena.mode.capturetheflag.ai.htn.Method
import bot.arena.mode.capturetheflag.ai.htn.SelectTwoNeutralFlagsTask
import bot.arena.mode.capturetheflag.ai.library.formations.LShapeFormation
import bot.arena.mode.capturetheflag.ai.library.formations.SimpleLineFormation
import bot.arena.mode.capturetheflag.ai.library.objectives.CaptureFlagObjective
import bot.arena.mode.capturetheflag.ai.library.objectives.ChargeTowersObjective
import bot.arena.mode.capturetheflag.ai.library.objectives.ScreenAreaObjective
import bot.arena.mode.capturetheflag.ai.library.tactics.AdvanceToTactic
import bot.arena.mode.capturetheflag.ai.library.tactics.CaptureFlagTactic
import bot.arena.mode.capturetheflag.ai.library.tactics.ChargeTowersTactic
import bot.arena.mode.capturetheflag.ai.library.tactics.HoldAreaTactic
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector
import bot.arena.mode.capturetheflag.ai.utility.GreedyAllocator
import bot.arena.mode.capturetheflag.ai.utility.Objective
import bot.arena.mode.capturetheflag.ai.utility.UtilityStrategyDirector
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.StructureTower

/**
 * "네가 말로 짠 전략"을 라이브러리/추상 인터페이스로 어떻게 표현할 수 있는지 보여주는 프리셋.
 *
 * 주의: 여기의 수치(요구 인원 등)는 예시다.
 * - Team A/B에 핏하게 하드코딩하는 게 목적이 아니라
 * - Objective/Hierarchical Task Network Task가 어떤 모양으로 생기면 좋은지 보여주는 초안이다.
 */

object CaptureTheFlagDirectors {

    /**
     * Utility AI 버전 Director
     */
    fun utility(roleOf: (Creep) -> Role): StrategyDirector {
        val objectives: List<Objective> = listOf(
            // 1) primary neutral flag 점령(소규모)
            CaptureFlagObjective(
                nameLabel = "primary",
                selectFlag = { worldModel, blackboard ->
                    blackboard.primaryTargetFlagId?.let(worldModel::getFlagById) ?: selectNeutralFlags(worldModel).firstOrNull()
                },
                combatRequirement = CapabilityRequest(minMelee = 1, minRanged = 1, minHeal = 1, maxSize = 3),
            ),
            // 2) primary flag 주변 스크린(시간 끌기)
            ScreenAreaObjective(
                nameLabel = "primaryHold",
                selectAnchor = { worldModel, blackboard ->
                    (blackboard.primaryTargetFlagId?.let(worldModel::getFlagById))?.let { Pos(it.x.toInt(), it.y.toInt()) }
                },
                requirement = CapabilityRequest(minMelee = 1, minHeal = 1, minRanged = 1, maxSize = 3),
            ),
            // 3) 타워 충전(워커)
            ChargeTowersObjective(
                nameLabel = "primaryTowers",
                selectTowers = { worldModel, blackboard ->
                    blackboard.primaryTargetFlagId?.let { towersControlledByFlag(worldModel, it) } ?: emptyList()
                },
                workerRequirement = CapabilityRequest(minWorker = 1, maxSize = 1),
            ),
            // 4) secondary neutral flag 점령(대규모)
            CaptureFlagObjective(
                nameLabel = "secondary",
                selectFlag = { worldModel, blackboard ->
                    blackboard.secondaryTargetFlagId?.let(worldModel::getFlagById) ?: selectNeutralFlags(worldModel).getOrNull(1)
                },
                combatRequirement = CapabilityRequest(minMelee = 2, minRanged = 2, minHeal = 2, maxSize = 9),
            ),
        )

        val allocator = GreedyAllocator(roleOf = roleOf, squadIdPrefix = "util")
        return UtilityStrategyDirector(objectives = objectives, allocator = allocator, maxActiveObjectives = 2)
    }

    /**
     * Hierarchical Task Network 버전 Director
     */
    fun hierarchicalTaskNetwork(): StrategyDirector {
        val root = object : CompoundTask {
            override val name: String = "CTFRoot"

            override fun methods(world: WorldModel, blackboard: Blackboard): List<Method> {
                return listOf(
                    Method(
                        name = "OpenWithSplitAndContest",
                        precondition = { _, _ -> true },
                        subtasks = listOf(
                            // 0) 타겟 플래그 2개 선정
                            SelectTwoNeutralFlagsTask(),
                            // 1) 소규모 스쿼드: primary로 진군 + 점령
                            AllocateSquadTask(
                                squadId = "hierarchicalTaskNetwork:B1",
                                requirement = CapabilityRequest(minMelee = 1, minHeal = 1, minRanged = 1, maxSize = 3),
                                tactic = CaptureFlagTactic(),
                                context = { worldModel, blackboard ->
                                    val targetFlag = blackboard.primaryTargetFlagId?.let(worldModel::getFlagById)
                                    val targetPosition = targetFlag?.let {
                                        bot.arena.mode.capturetheflag.ai.core.Pos(it.x.toInt(), it.y.toInt())
                                    }
                                    bot.arena.mode.capturetheflag.ai.core.TacticContext(
                                        targetFlagId = targetFlag?.id?.toString(),
                                        targetPos = targetPosition,
                                        formation = LShapeFormation(),
                                    )
                                }
                            ),
                            // 2) 워커: primary 연계 타워 충전
                            AllocateSquadTask(
                                squadId = "hierarchicalTaskNetwork:B2",
                                requirement = CapabilityRequest(minWorker = 1, maxSize = 1),
                                tactic = ChargeTowersTactic(),
                                context = { worldModel, blackboard ->
                                    val targetFlagId = blackboard.primaryTargetFlagId
                                    val towers = if (targetFlagId != null) {
                                        towersControlledByFlag(worldModel, targetFlagId)
                                    } else {
                                        emptyList()
                                    }
                                    val anchor = towers.firstOrNull()?.let { bot.arena.mode.capturetheflag.ai.core.Pos(it.x.toInt(), it.y.toInt()) }
                                    bot.arena.mode.capturetheflag.ai.core.TacticContext(
                                        targetTowerIds = towers.map { it.id.toString() },
                                        targetPos = anchor,
                                    )
                                }
                            ),
                            // 3) 대규모 스쿼드: secondary로 진군/교전
                            AllocateSquadTask(
                                squadId = "hierarchicalTaskNetwork:A1",
                                requirement = CapabilityRequest(minMelee = 2, minHeal = 2, minRanged = 2, maxSize = 9),
                                tactic = AdvanceToTactic(),
                                context = { worldModel, blackboard ->
                                    val targetFlag = blackboard.secondaryTargetFlagId?.let(worldModel::getFlagById)
                                    val targetPosition = targetFlag?.let {
                                        bot.arena.mode.capturetheflag.ai.core.Pos(it.x.toInt(), it.y.toInt())
                                    }
                                    bot.arena.mode.capturetheflag.ai.core.TacticContext(
                                        targetFlagId = targetFlag?.id?.toString(),
                                        targetPos = targetPosition,
                                        formation = SimpleLineFormation(width = 3),
                                    )
                                }
                            ),
                        )
                    )
                )
            }
        }

        return HierarchicalTaskNetworkStrategyDirector(root = root)
    }

    // ------------------------------
    // Selector helpers
    // ------------------------------

    private fun selectNeutralFlags(world: WorldModel): List<Flag> =
        world.flags.filter { it.my == null }.sortedBy { it.x }

    private fun towersControlledByFlag(world: WorldModel, flagId: String): List<StructureTower> =
        world.towers.filter { it.controlledById == flagId }
}
