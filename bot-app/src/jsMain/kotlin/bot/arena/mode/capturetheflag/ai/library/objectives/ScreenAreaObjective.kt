package bot.arena.mode.capturetheflag.ai.library.objectives

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.library.tactics.HoldAreaTactic
import bot.arena.mode.capturetheflag.ai.utility.Objective
import bot.arena.mode.capturetheflag.ai.utility.TaskRequest

/**
 * 특정 구역에서 "시간 끌기/버티기" 목표.
 */
class ScreenAreaObjective(
    private val nameLabel: String,
    private val selectAnchor: (WorldModel, Blackboard) -> Pos?,
    private val requirement: CapabilityRequest,
) : Objective {
    override val name: String = "ScreenArea($nameLabel)"

    override fun priority(world: WorldModel, blackboard: Blackboard): Double {
        val anchor = selectAnchor(world, blackboard) ?: return 0.0
        val threat = world.enemiesInRange(anchor.toHasPosition(), 10).size
        // 적이 가까울수록 우선순위를 올림
        return 10.0 + threat * 20.0
    }

    override fun proposeTasks(world: WorldModel, blackboard: Blackboard): List<TaskRequest> {
        val anchor = selectAnchor(world, blackboard) ?: return emptyList()
        return listOf(
            TaskRequest(
                id = "screen:$nameLabel",
                capability = requirement,
                tactic = HoldAreaTactic(),
                context = TacticContext(targetPos = anchor)
            )
        )
    }
}
