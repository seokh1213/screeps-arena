package bot.arena.mode.capturetheflag.memory

import bot.arena.common.memory.CreepMemory
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.CreepContext
import bot.arena.mode.capturetheflag.model.Phase
import screeps.bindings.arena.Creep

class Memory {
    var beforeState: Pair<Phase, Context>
        private set
    var currentState: Pair<Phase, Context>
        private set

    val beforePhase: Phase get() = beforeState.first
    val currentPhase: Phase get() = currentState.first
    val beforeContext: Context get() = beforeState.second
    val currentContext: Context get() = currentState.second

    val creepMemory = CreepMemory<CreepContext>()

    init {
        val state = Phase.START to Context.START
        beforeState = state
        currentState = state
    }

    fun updateState(phase: Phase, context: Context) {
        beforeState = currentState
        currentState = phase to context
    }

    val myCreeps: List<Creep> get() = currentContext.myCreeps
    val enemyCreeps: List<Creep> get() = currentContext.enemyCreeps

    fun getMyCreepsByRole(select: (CreepContext) -> Boolean): List<Creep> {
        val myCreeps = currentContext.myCreeps.withContext()
        return myCreeps.filter { (_, context) -> context != null && select(context) }
            .map { (creep) -> creep }
    }

    fun getMyCreepsWithContext() = currentContext.myCreeps.withContext()

    private fun List<Creep>.withContext() = map { creep ->
        creep to creepMemory[creep]
    }
}
