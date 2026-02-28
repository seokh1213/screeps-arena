package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.GameObject
import screeps.bindings.arena.HasPosition

data class Instruction<T : GameObject>(
    val target: HasPosition,
    val instruction: T.() -> Unit,
)
