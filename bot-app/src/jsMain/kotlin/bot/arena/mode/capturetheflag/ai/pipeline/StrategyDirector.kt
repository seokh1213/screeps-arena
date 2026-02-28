package bot.arena.mode.capturetheflag.ai.pipeline

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.WorldModel

/**
 * StrategyDirector = "이번 틱에 무엇을 할지"를 정하는 상위 의사결정자.
 *
 * - Utility AI 버전: Objective를 점수화해서 최우선 목표들을 뽑고 계획을 만든다.
 * - Hierarchical Task Network 버전: Root Task를 분해해서 실행 가능한 primitive plan을 만든다.
 */
fun interface StrategyDirector {
    fun tick(world: WorldModel, blackboard: Blackboard, groups: GroupStore): StrategyPlan
}
