package bot.arena.mode.capturetheflag.ai.library.hsm

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.arena.Creep

/**
 * HSM(계층 상태 머신) 기반 전술을 만들기 위한 베이스.
 *
 * 핵심 아이디어:
 * - Tactic 인스턴스는 매 틱 새로 만들어질 수 있으니, 상태는 [Blackboard]에 저장한다.
 * - 상태 전이 규칙을 한 곳(runState)에서 관리하면,
 *   "언제 다음 단계로 넘어가는가"가 코드 구조에 자연스럽게 드러난다.
 */
abstract class StatefulTactic(
    private val stateKey: String = "hsm.state"
) : Tactic {

    protected abstract val initialState: String

    /**
     * @return nextState
     */
    protected abstract fun runState(
        state: String,
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ): String

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        val state = blackboard.getSquadValue<String>(squadId, stateKey) ?: initialState
        val next = runState(state, squadId, members, world, blackboard, tacticContext, outputIntents)
        blackboard.setSquadValue(squadId, stateKey, next)
    }
}
