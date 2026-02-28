package bot.arena.mode.capturetheflag

import bot.arena.mode.capturetheflag.memory.Memory
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.CreepContext
import bot.arena.mode.capturetheflag.model.Order
import bot.arena.mode.capturetheflag.model.Phase
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.ATTACK
import screeps.bindings.CARRY
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.game.getObjectsByPrototype
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

class Overmind {
    private val memory = Memory()

    init {
        updateMemory()
        assignCreepRole(memory.currentContext)
    }

    fun commandOrder(): List<Order<*>> {
        updateMemory()
        return makeOrders()
    }

    private fun updateMemory() {
        val context = getCurrentContext()
        val phase = evaluatePhase(context)

        memory.updateState(phase, context)
    }

    private fun getCurrentContext() = Context(
        creeps = getObjectsByPrototype(Creep),
        flags = getObjectsByPrototype(Flag),
        bodyPartItems = getObjectsByPrototype(BodyPart)
    )

    /**
     * 두뇌 역할 - 현재와 이전의 상태를 보고 판단
     */
    private fun evaluatePhase(currentContext: Context): Phase {
        val (beforePhase, beforeContext) = memory.beforeState

        return Phase.INITIAL
    }

    private fun makeOrders(): List<Order<*>> {
        return emptyList()
    }

    private fun assignCreepRole(context: Context) {
        val myCreeps = context.myCreeps

        myCreeps.forEach { creep ->
            val creepContext = memory.creepMemory[creep]
            if (creepContext?.id?.isNotEmpty() == true) {
                return@forEach
            }

            memory.creepMemory[creep] = CreepContext(
                id = creep.id,
                role = creep.determineRole()
            )
        }
    }

    private fun Creep.determineRole(): Role {
        val bodyParts = body.map { it.type }
        return when {
            bodyParts.contains(CARRY) -> Role.WORKER
            bodyParts.contains(HEAL) -> Role.HEALER
            bodyParts.contains(RANGED_ATTACK) -> Role.RANGER
            bodyParts.contains(ATTACK) -> Role.MELEE
            else -> Role.CREEP
        }
    }
}
