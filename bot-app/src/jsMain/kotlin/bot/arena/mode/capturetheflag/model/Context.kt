package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

data class Context(
    val creeps: List<Creep>,
    val flags: List<Flag>,
    val bodyPartItems: List<BodyPart>,
) {
    constructor(
        creeps: Array<Creep>,
        flags: Array<Flag>,
        bodyPartItems: Array<BodyPart>,
    ) : this(
        creeps = creeps.toList(),
        flags = flags.toList(),
        bodyPartItems = bodyPartItems.toList(),
    )

    val myCreeps = creeps.filter { it.my }
    val enemyCreeps = creeps.filter { !it.my }

    companion object {
        val START = Context(
            creeps = emptyList(),
            flags = emptyList(),
            bodyPartItems = emptyList(),
        )
    }
}

data class CreepContext(
    val id: String,
    val role: Role,
)