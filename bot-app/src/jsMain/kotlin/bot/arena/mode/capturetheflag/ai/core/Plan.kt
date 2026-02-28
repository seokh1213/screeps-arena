package bot.arena.mode.capturetheflag.ai.core

/**
 * StrategyDirector가 한 틱에 만들어내는 결과물.
 *
 * - operations: 그룹을 어떻게 나눌지/합칠지/재배치할지
 * - assignments: 각 스쿼드가 어떤 전술을 수행할지
 */
data class StrategyPlan(
    val operations: List<GroupOperation> = emptyList(),
    val assignments: List<TacticAssignment> = emptyList(),
)

data class TacticAssignment(
    val squadId: String,
    val tactic: Tactic,
    val context: TacticContext = TacticContext(),
)
