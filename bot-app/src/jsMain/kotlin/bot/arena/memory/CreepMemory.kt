package bot.arena.memory

import screeps.bindings.arena.Creep
import screeps.bindings.arena.game.PrototypeCreep
import screeps.bindings.arena.game.getObjectsByPrototype

/**
 * Screeps Arena에는 World의 Memory 시스템이 없으므로,
 * JS 힙(Kotlin 객체는 틱 간 살아있음)을 이용해 creep role을 관리한다.
 *
 * role은 임의의 [String]을 사용할 수 있다.
 * 자주 쓰는 이름은 [CreepRoles]에 상수로 정의되어 있지만, 사용 강제는 없다.
 */
object CreepMemory {

    private val roles = mutableMapOf<String, String>()

    fun get(creep: Creep): String? = roles[creep.id.toString()]

    fun set(creep: Creep, role: String) {
        roles[creep.id.toString()] = role
    }

    /**
     * 특정 role의 살아있는 내 creep 수를 반환한다.
     * 죽은 creep(exists=false)의 데이터는 자동으로 정리된다.
     */
    fun countAlive(role: String): Int {
        val aliveIds = getObjectsByPrototype(PrototypeCreep)
            .filter { it.my && it.exists }
            .map { it.id.toString() }
            .toSet()

        roles.keys.retainAll(aliveIds)

        return roles.values.count { it == role }
    }
}

object CreepRoles {
    const val WORKER = "worker"
    const val ATTACKER = "attacker"
}
