package bot.arena.mode.capturetheflag.ai.utility

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector

/**
 * Utility AI Director
 *
 * - Objective들을 priority로 정렬
 * - 상위 N개 목표의 TaskRequest를 모아서
 * - Allocator로 스쿼드 생성 + 전술 할당
 */
class UtilityStrategyDirector(
    private val objectives: List<Objective>,
    private val allocator: GreedyAllocator,
    private val maxActiveObjectives: Int = 2,
) : StrategyDirector {

    override fun tick(world: WorldModel, blackboard: Blackboard, groups: GroupStore): StrategyPlan {
        val ranked = objectives
            .map { it to it.priority(world, blackboard) }
            .filter { (_, p) -> p > 0.0 }
            .sortedByDescending { (_, p) -> p }
            .take(maxActiveObjectives)

        val tasks = ranked.flatMap { (objective, _) -> objective.proposeTasks(world, blackboard) }

        // 초안: 매 틱 새로 할당 (고급화 시 기존 groups를 참고해서 안정화)
        return allocator.allocate(world, tasks)
    }
}
