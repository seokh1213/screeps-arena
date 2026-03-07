package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.Creep

interface Group {
    val id: String
}

class Team(
    override val id: String,
    val squads: List<Squad>
) : Group

class Squad(
    override val id: String,
    val creeps: List<Creep>
) : Group