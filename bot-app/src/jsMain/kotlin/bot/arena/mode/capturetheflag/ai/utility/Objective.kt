package bot.arena.mode.capturetheflag.ai.utility

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.WorldModel

/**
 * Utility AI에서 목표(Objective)는 "해야 할 일(TaskRequest)"을 제안하고,
 * Director는 점수(priority)에 따라 어떤 목표를 활성화할지 고른다.
 */
interface Objective {
    val name: String

    /**
     * 지금 이 목표를 수행할 가치(우선순위).
     * - 0 이하: 비활성 취급
     */
    fun priority(world: WorldModel, blackboard: Blackboard): Double

    /**
     * 목표를 수행하기 위해 필요한 작업 요청들.
     */
    fun proposeTasks(world: WorldModel, blackboard: Blackboard): List<TaskRequest>
}

data class TaskRequest(
    val id: String,
    val capability: CapabilityRequest,
    val tactic: Tactic,
    val context: TacticContext,
    /**
     * 같은 task를 계속 수행하는 동안, 스쿼드를 재구성하지 않도록(진동 방지) 힌트.
     * 초안에서는 아직 강제하지 않음.
     */
    val stickyKey: String? = null,
)
