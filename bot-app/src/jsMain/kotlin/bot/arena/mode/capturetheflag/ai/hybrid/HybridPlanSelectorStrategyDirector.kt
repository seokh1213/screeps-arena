package bot.arena.mode.capturetheflag.ai.hybrid

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.htn.HierarchicalTaskNetworkPlanner
import bot.arena.mode.capturetheflag.ai.htn.HierarchicalTaskNetworkTask
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector

/**
 * Hybrid A:
 * - Hierarchical Task Network 플랜 후보를 여러 개 만들고
 * - Utility 점수로 후보를 선택
 * - switchCost / minLockTicks / interrupt로 진동을 완화
 */
class HybridPlanSelectorStrategyDirector(
    private val candidates: List<CandidatePlan>,
    private val planner: HierarchicalTaskNetworkPlanner = HierarchicalTaskNetworkPlanner(),
    private val switchCost: Double = 18.0,
    private val minLockTicks: Int = 10,
    private val interrupt: InterruptRule = { _, _, _, _ -> false },
) : StrategyDirector {

    private var tickCounter: Int = 0
    private var activePlanId: String? = null
    private var lockUntilTick: Int = 0

    override fun tick(world: WorldModel, blackboard: Blackboard, groups: GroupStore): StrategyPlan {
        tickCounter += 1

        val evaluated = candidates
            .map { candidate ->
                val plan = planner.plan(candidate.rootTask, world, blackboard)
                val utility = candidate.utilityScore(world, blackboard, groups)
                val readinessBonus = planReadinessBonus(plan)
                EvaluatedPlan(
                    id = candidate.id,
                    plan = plan,
                    score = utility + readinessBonus,
                )
            }
            .filter { it.plan.assignments.isNotEmpty() || it.plan.operations.isNotEmpty() }

        if (evaluated.isEmpty()) return StrategyPlan()

        val active = activePlanId?.let { current -> evaluated.firstOrNull { it.id == current } }
        val lockIsActive = tickCounter < lockUntilTick
        val interruptRequested = interrupt(world, blackboard, groups, activePlanId)

        if (lockIsActive && !interruptRequested && active != null) {
            return active.plan
        }

        val chosen = evaluated.maxByOrNull { candidate ->
            val switchingPenalty = if (activePlanId != null && activePlanId != candidate.id) switchCost else 0.0
            val currentPlanBonus = if (activePlanId == candidate.id) 2.0 else 0.0
            candidate.score - switchingPenalty + currentPlanBonus
        } ?: return StrategyPlan()

        if (chosen.id != activePlanId) {
            activePlanId = chosen.id
            lockUntilTick = tickCounter + minLockTicks
        }

        return chosen.plan
    }

    private fun planReadinessBonus(plan: StrategyPlan): Double {
        val assignmentBonus = plan.assignments.size * 4.0
        val operationBonus = plan.operations.size * 2.0
        return assignmentBonus + operationBonus
    }
}

data class CandidatePlan(
    val id: String,
    val rootTask: HierarchicalTaskNetworkTask,
    val utilityScore: (WorldModel, Blackboard, GroupStore) -> Double,
)

typealias InterruptRule = (WorldModel, Blackboard, GroupStore, String?) -> Boolean

private data class EvaluatedPlan(
    val id: String,
    val plan: StrategyPlan,
    val score: Double,
)
