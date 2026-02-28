package bot.arena.mode.capturetheflag.ai.htn

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector

/**
 * Hierarchical Task Network Director
 * - root task를 매 틱 re-plan(초안)
 * - 만들어진 StrategyPlan을 그대로 Coordinator에게 전달
 */
class HierarchicalTaskNetworkStrategyDirector(
    private val root: HierarchicalTaskNetworkTask,
    private val planner: HierarchicalTaskNetworkPlanner = HierarchicalTaskNetworkPlanner(),
) : StrategyDirector {

    override fun tick(world: WorldModel, blackboard: Blackboard, groups: GroupStore): StrategyPlan {
        return planner.plan(root, world, blackboard)
    }
}
