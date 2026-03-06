package bot.arena.mode.capturetheflag.ai.pipeline

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.WorldModel

/**
 * StrategyDirector = "이번 틱에 무엇을 할지"를 정하는 상위 의사결정자.
 *
 * - 현재 기본 구현은 Hybrid(Utility 점수로 Hierarchical Task Network 플랜 후보 선택) 방식이다.
 * - 결과물은 GroupOperation + TacticAssignment로 구성된 StrategyPlan이다.
 */
fun interface StrategyDirector {
    fun tick(world: WorldModel, blackboard: Blackboard, groups: GroupStore): StrategyPlan
}
