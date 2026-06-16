package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

data class Context(
    val creeps: List<Creep>,
    val flags: List<Flag>,
    val bodyPartItems: List<BodyPart>,
    val towers: List<StructureTower>,
    val containers: List<StructureContainer>,
) {
    val myCreeps = creeps.filter { it.my }
    val enemyCreeps = creeps.filter { !it.my }

    val myFlags = flags.filter { it.my == true }
    val enemyFlags = flags.filter { it.my == false }
    val neutralFlags = flags.filter { it.my == null }

    val myTowers = towers.filter { it.my == true }
    val enemyTowers = towers.filter { it.my == false }
    val neutralTowers = towers.filter { it.my == null }

    companion object {
        val START = Context(
            creeps = emptyList(),
            flags = emptyList(),
            bodyPartItems = emptyList(),
            towers = emptyList(),
            containers = emptyList(),
        )
    }
}

data class CreepContext(
    val id: String,
    val role: Role,
)
