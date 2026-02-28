package bot.arena.mode.capturetheflag.ai.htn

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.TacticAssignment
import bot.arena.mode.capturetheflag.ai.core.GroupOperation
import bot.arena.mode.capturetheflag.ai.core.WorldModel

/**
 * Hierarchical Task Network(계층적 태스크 네트워크) 초안.
 *
 * - CompoundTask는 method(분해 규칙) 목록을 가지고,
 *   현재 월드/블랙보드에 적용 가능한 method를 선택해 하위 태스크로 분해한다.
 * - PrimitiveTask는 StrategyPlan에 직접 기여(스쿼드 생성/전술 할당 등).
 */

sealed interface HierarchicalTaskNetworkTask {
    val name: String
}

interface PrimitiveTask : HierarchicalTaskNetworkTask {
    fun apply(world: WorldModel, blackboard: Blackboard, out: PlanBuilder)
}

interface CompoundTask : HierarchicalTaskNetworkTask {
    fun methods(world: WorldModel, blackboard: Blackboard): List<Method>
}

data class Method(
    val name: String,
    val precondition: (WorldModel, Blackboard) -> Boolean,
    val subtasks: List<HierarchicalTaskNetworkTask>,
)

class PlanBuilder {
    private val ops = mutableListOf<GroupOperation>()
    private val assigns = mutableListOf<TacticAssignment>()

    /**
     * Hierarchical Task Network 플래닝 중 중복 배치를 막기 위한 임시 예약 집합.
     * - 초안에서는 단순히 "한 플랜 내에서"만 유효
     */
    val reservedCreepIds: MutableSet<String> = mutableSetOf()

    fun op(operation: GroupOperation) { ops += operation }
    fun assign(assignment: TacticAssignment) { assigns += assignment }

    fun build(): StrategyPlan = StrategyPlan(operations = ops.toList(), assignments = assigns.toList())
}

class HierarchicalTaskNetworkPlanner(
    private val maxDepth: Int = 32,
) {
    fun plan(root: HierarchicalTaskNetworkTask, world: WorldModel, blackboard: Blackboard): StrategyPlan {
        val builder = PlanBuilder()
        val ok = expand(root, world, blackboard, builder, depth = 0)
        return if (ok) builder.build() else StrategyPlan()
    }

    private fun expand(task: HierarchicalTaskNetworkTask, world: WorldModel, blackboard: Blackboard, out: PlanBuilder, depth: Int): Boolean {
        if (depth > maxDepth) return false
        return when (task) {
            is PrimitiveTask -> {
                task.apply(world, blackboard, out)
                true
            }
            is CompoundTask -> {
                val method = task.methods(world, blackboard).firstOrNull { it.precondition(world, blackboard) } ?: return false
                method.subtasks.all { sub -> expand(sub, world, blackboard, out, depth + 1) }
            }
        }
    }
}
