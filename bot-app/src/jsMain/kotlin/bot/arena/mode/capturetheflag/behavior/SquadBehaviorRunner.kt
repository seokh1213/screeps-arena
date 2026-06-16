package bot.arena.mode.capturetheflag.behavior

import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.arena.Creep

class SquadBehaviorRunner(
    val squadId: String,
    initialState: SquadState,
) {
    var currentState: SquadState = initialState
        private set

    fun tick(creeps: List<Creep>, ctx: Context): List<Order<Creep>> {
        if (creeps.isEmpty()) return emptyList()
        val result = currentState.evaluate(creeps, ctx)
        currentState = result.nextState
        return result.orders
    }
}
