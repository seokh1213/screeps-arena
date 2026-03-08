package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.Source
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

data class Context(
    val creeps: List<Creep>,
    val flags: List<Flag>,
    val bodyPartItems: List<BodyPart>,
    val towers: List<StructureTower>,
    val containers: List<StructureContainer>,
    val sources: List<Source>,
) {
    constructor(
        creeps: Array<Creep>,
        flags: Array<Flag>,
        bodyPartItems: Array<BodyPart>,
        towers: Array<StructureTower>,
        containers: Array<StructureContainer>,
        sources: Array<Source>,
    ) : this(
        creeps = creeps.toList(),
        flags = flags.toList(),
        bodyPartItems = bodyPartItems.toList(),
        towers = towers.toList(),
        containers = containers.toList(),
        sources = sources.toList(),
    )

    val myCreeps = creeps.filter { it.my && !it.spawning }
    val enemyCreeps = creeps.filter { !it.my && !it.spawning }
    val myFlags = flags.filter { it.my == true }
    val enemyFlags = flags.filter { it.my == false }
    val neutralFlags = flags.filter { it.my == null }
    val myTowers = towers.filter { it.my == true }
    val enemyTowers = towers.filter { it.my == false }
    val myContainers = containers.filter { it.my == true }
    val enemyContainers = containers.filter { it.my == false }

    companion object {
        val START = Context(
            creeps = emptyList(),
            flags = emptyList(),
            bodyPartItems = emptyList(),
            towers = emptyList(),
            containers = emptyList(),
            sources = emptyList(),
        )
    }
}

data class CreepContext(
    val id: String,
    val role: Role,
)
