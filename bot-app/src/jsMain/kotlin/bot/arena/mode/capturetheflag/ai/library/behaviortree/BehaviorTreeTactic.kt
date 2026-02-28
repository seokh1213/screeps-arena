package bot.arena.mode.capturetheflag.ai.library.behaviortree

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.arena.Creep

/**
 * Behavior Tree 공용 타입과, 전술(Tactic) 어댑터를 한 파일로 묶었다.
 *
 * - BehaviorTree 풀네임을 사용.
 * - 기존 두 파일 분리를 줄여 탐색 비용을 낮춤.
 */
enum class BehaviorTreeStatus { Success, Failure, Running }

data class BehaviorTreeContext(
    val squadId: String,
    val members: List<Creep>,
    val world: WorldModel,
    val blackboard: Blackboard,
    val tacticContext: TacticContext,
    val outputIntents: MutableMap<String, UnitIntent>,
)

fun interface BehaviorTreeNode {
    fun tick(context: BehaviorTreeContext): BehaviorTreeStatus
}

class Sequence(private vararg val children: BehaviorTreeNode) : BehaviorTreeNode {
    override fun tick(context: BehaviorTreeContext): BehaviorTreeStatus {
        for (child in children) {
            when (child.tick(context)) {
                BehaviorTreeStatus.Success -> continue
                BehaviorTreeStatus.Running -> return BehaviorTreeStatus.Running
                BehaviorTreeStatus.Failure -> return BehaviorTreeStatus.Failure
            }
        }
        return BehaviorTreeStatus.Success
    }
}

class Selector(private vararg val children: BehaviorTreeNode) : BehaviorTreeNode {
    override fun tick(context: BehaviorTreeContext): BehaviorTreeStatus {
        for (child in children) {
            when (child.tick(context)) {
                BehaviorTreeStatus.Success -> return BehaviorTreeStatus.Success
                BehaviorTreeStatus.Running -> return BehaviorTreeStatus.Running
                BehaviorTreeStatus.Failure -> continue
            }
        }
        return BehaviorTreeStatus.Failure
    }
}

class Condition(
    private val predicate: (BehaviorTreeContext) -> Boolean
) : BehaviorTreeNode {
    override fun tick(context: BehaviorTreeContext): BehaviorTreeStatus {
        return if (predicate(context)) BehaviorTreeStatus.Success else BehaviorTreeStatus.Failure
    }
}

class ActionNode(
    private val action: (BehaviorTreeContext) -> BehaviorTreeStatus
) : BehaviorTreeNode {
    override fun tick(context: BehaviorTreeContext): BehaviorTreeStatus = action(context)
}

class BehaviorTreeTactic(
    override val name: String,
    private val root: BehaviorTreeNode,
) : Tactic {

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        root.tick(
            BehaviorTreeContext(
                squadId = squadId,
                members = members,
                world = world,
                blackboard = blackboard,
                tacticContext = tacticContext,
                outputIntents = outputIntents,
            )
        )
    }
}
