package bot.arena.mode.capturetheflag.behavior

import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.arena.Creep

interface SquadState {
    fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult
}

data class SquadTickResult(
    val orders: List<Order<Creep>>,
    val nextState: SquadState,
)

fun interface BehaviorFactory {
    fun createInitialState(ctx: Context, creeps: List<Creep>): SquadState
}
