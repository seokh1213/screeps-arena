package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.Creep

interface Allocator {
    val id: String
}

class RoleAllocator<T>(
    private val creepRoleFactory: CreepRoleFactory<T>
) : Allocator where T : CreepRoleEnum, T : Enum<T> {
    override val id = "role-allocator"

    private val cache = mutableMapOf<String, T>()

    fun getRole(creep: Creep): T {
        val id = creep.id

        return cache.getOrPut(id) {
            creepRoleFactory.assign(creep)
        }
    }

    fun assignRoles(creeps: List<Creep>): List<Pair<Creep, T>> {
        return creeps.map { it to getRole(it) }
    }
}


class TeamAllocator<T>(
    override val id: String,
    val squadAllocators: List<SquadAllocator<T>>
) : Allocator where T : CreepRoleEnum, T : Enum<T>


class SquadAllocator<T>(
    override val id: String,
    val squadSpec: Map<T, Int>
) : Allocator where T : CreepRoleEnum, T : Enum<T>


interface CreepRoleEnum

interface CreepRoleFactory<T> where T : CreepRoleEnum, T : Enum<T> {
    fun assign(creep: Creep): T
}