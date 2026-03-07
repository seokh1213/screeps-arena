package bot.arena.mode.capturetheflag.model

import screeps.bindings.ATTACK
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.WORK
import screeps.bindings.arena.Creep

interface StrategyPlan<T> where T : CreepRoleEnum, T : Enum<T> {
    val teamAllocators: List<TeamAllocator<T>>
    val roleAllocator: RoleAllocator<T>

    fun allocateTeam(creeps: List<Creep>): List<Team> {
        val creepWithRoles = roleAllocator.assignRoles(creeps)

        val remainCreeps: MutableMap<T, MutableList<Creep>> = creepWithRoles.groupBy({ it.second }, { it.first })
            .mapValues { it.value.toMutableList() }
            .toMutableMap()

        return teamAllocators.mapNotNull {
            val needCreepRoles = it.squadAllocators.flatMap { it.squadSpec.entries }
                .groupBy({ it.key }, { it.value })
                .mapValues { it.value.sum() }

            // 조건을 충족하나 탐색
            val canCreateSquads = needCreepRoles.all { (role, cnt) ->
                (remainCreeps[role]?.size ?: 0) >= cnt
            }

            if (!canCreateSquads) {
                return@mapNotNull null
            }

            val squads = it.squadAllocators.map { squadAllocator ->
                val creeps = squadAllocator.squadSpec.flatMap { (role, size) ->
                    remainCreeps[role]!!.take(size).also {
                        remainCreeps[role]!!.removeAll(it)
                    }
                }

                Squad(
                    id = squadAllocator.id,
                    creeps = creeps,
                )
            }

            Team(
                id = it.id,
                squads = squads
            )
        }
    }
}


enum class CTFCreepRole : CreepRoleEnum {
    WORKER,
    MELEE,
    RANGER,
    HEALER,
    NONE
}

class CTFRoleFactory : CreepRoleFactory<CTFCreepRole> {
    override fun assign(creep: Creep): CTFCreepRole {
        val bodyTypes = creep.body.map { it.type }

        return when {
            bodyTypes.contains(WORK) -> CTFCreepRole.WORKER
            bodyTypes.contains(ATTACK) -> CTFCreepRole.MELEE
            bodyTypes.contains(RANGED_ATTACK) -> CTFCreepRole.RANGER
            bodyTypes.contains(HEAL) -> CTFCreepRole.HEALER
            else -> CTFCreepRole.NONE
        }
    }
}

class AsymmetricFlagCapture : StrategyPlan<CTFCreepRole> {
    override val roleAllocator = RoleAllocator(CTFRoleFactory())

    override val teamAllocators = listOf(
        TeamAllocator(
            id = "Team-A",
            squadAllocators = listOf(
                SquadAllocator(
                    id = "Worker",
                    squadSpec = mapOf(
                        CTFCreepRole.WORKER to 1,
                    )
                ),
                SquadAllocator(
                    id = "Attacker",
                    squadSpec = mapOf(
                        CTFCreepRole.MELEE to 1,
                        CTFCreepRole.RANGER to 1,
                        CTFCreepRole.HEALER to 1,
                    )
                ),
            )
        ),
        TeamAllocator(
            id = "Team-B",
            squadAllocators = listOf(
                SquadAllocator(
                    id = "Worker",
                    squadSpec = mapOf(
                        CTFCreepRole.WORKER to 1,
                    )
                ),
                SquadAllocator(
                    id = "Attacker",
                    squadSpec = mapOf(
                        CTFCreepRole.MELEE to 3,
                        CTFCreepRole.RANGER to 3,
                        CTFCreepRole.HEALER to 3,
                    )
                ),
            )
        )
    )

}