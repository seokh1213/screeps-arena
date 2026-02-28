package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.GameObject

data class Order<T : GameObject>(
    val performer: T,
    val instructions: Instructions<T>
) {
    fun perform() {
        instructions.forEach { instruction ->
            instruction(performer)
        }
    }
}
