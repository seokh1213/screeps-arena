package bot.arena.mode.capturetheflag.ai.library.objectives

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.library.tactics.CaptureFlagTactic
import bot.arena.mode.capturetheflag.ai.utility.Objective
import bot.arena.mode.capturetheflag.ai.utility.TaskRequest
import screeps.bindings.arena.Flag

/**
 * "특정 플래그를 점령/유지" 목표.
 *
 * - 어떤 플래그를 노릴지는 외부에서 selector로 주입.
 * - Team A/B에 맞춘 하드코딩이 아니라, 같은 objective를 어디든 재사용.
 */
class CaptureFlagObjective(
    private val nameLabel: String,
    private val selectFlag: (WorldModel, Blackboard) -> Flag?,
    private val combatRequirement: CapabilityRequest,
) : Objective {
    override val name: String = "CaptureFlag($nameLabel)"

    override fun priority(world: WorldModel, blackboard: Blackboard): Double {
        val flag = selectFlag(world, blackboard) ?: return 0.0
        // 내 것이 아니면 높게, 이미 내 것이면 낮게
        return when (flag.my) {
            true -> 10.0
            false -> 100.0
            null -> 80.0
            else -> 50.0
        }
    }

    override fun proposeTasks(world: WorldModel, blackboard: Blackboard): List<TaskRequest> {
        val flag = selectFlag(world, blackboard) ?: return emptyList()
        val pos = Pos(flag.x.toInt(), flag.y.toInt())
        return listOf(
            TaskRequest(
                id = "capture:$nameLabel",
                capability = combatRequirement,
                tactic = CaptureFlagTactic(),
                context = TacticContext(
                    targetFlagId = flag.id.toString(),
                    targetPos = pos,
                )
            )
        )
    }
}
