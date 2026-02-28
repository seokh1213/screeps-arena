package bot.arena.mode.capturetheflag.ai.library.objectives

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.library.tactics.ChargeTowersTactic
import bot.arena.mode.capturetheflag.ai.utility.Objective
import bot.arena.mode.capturetheflag.ai.utility.TaskRequest
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.StructureTower
import screeps.bindings.getUsedCapacity

/**
 * 타워(들) 에너지 보급 목표.
 *
 * - 어떤 타워를 충전할지는 selector로 주입.
 */
class ChargeTowersObjective(
    private val nameLabel: String,
    private val selectTowers: (WorldModel, Blackboard) -> List<StructureTower>,
    private val workerRequirement: CapabilityRequest = CapabilityRequest(minWorker = 1, maxSize = 1),
) : Objective {
    override val name: String = "ChargeTowers($nameLabel)"

    override fun priority(world: WorldModel, blackboard: Blackboard): Double {
        val towers = selectTowers(world, blackboard)
        if (towers.isEmpty()) return 0.0
        // 에너지가 비어있을수록 우선순위를 올림(초안)
        val avgUsed = towers.map { it.store.getUsedCapacity(RESOURCE_ENERGY.toString()) ?: 0 }.average()
        return if (avgUsed <= 0.0) 60.0 else 20.0
    }

    override fun proposeTasks(world: WorldModel, blackboard: Blackboard): List<TaskRequest> {
        val towers = selectTowers(world, blackboard)
        if (towers.isEmpty()) return emptyList()
        val towerIds = towers.map { it.id.toString() }
        val anchor = towers.first().let { Pos(it.x.toInt(), it.y.toInt()) }

        return listOf(
            TaskRequest(
                id = "charge:$nameLabel",
                capability = workerRequirement,
                tactic = ChargeTowersTactic(),
                context = TacticContext(
                    targetTowerIds = towerIds,
                    targetPos = anchor,
                )
            )
        )
    }
}
