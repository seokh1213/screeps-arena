package bot.arena.common.memory

import screeps.bindings.arena.Creep

class CreepMemory<T> {

    private val memories = mutableMapOf<String, T>()

    operator fun get(creep: Creep): T? = memories[creep.id.toString()]

    operator fun set(creep: Creep, memory: T) {
        memories[creep.id.toString()] = memory
    }

    fun retainAll(creeps: Collection<Creep>) {
        memories.keys.retainAll(creeps.map { it.id.toString() }.toSet())
    }

    fun count(target: T): Int = memories.values.count { it == target }

    fun <S> count(target: S, select: (T) -> S): Int = memories.values.count { target == select(it) }
}
