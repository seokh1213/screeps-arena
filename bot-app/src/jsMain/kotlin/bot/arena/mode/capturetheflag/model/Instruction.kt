package bot.arena.mode.capturetheflag.model

import screeps.bindings.arena.GameObject

data class Instructions<T : GameObject>(
    val instructions: List<Instruction<T>>,
) : List<Instruction<T>> by instructions {
    constructor(vararg instructions: Instruction<T>) : this(instructions.toList())

    companion object {
        operator fun <T : GameObject> invoke(
            block: Builder<T>.() -> Unit,
        ): Instructions<T> = Builder<T>().apply(block).build()
    }

    class Builder<T : GameObject> {
        private val items = mutableListOf<Instruction<T>>()

        fun instruction(action: T.() -> Unit) {
            items += Instruction { performer ->
                performer.action()
            }
        }

        internal fun build(): Instructions<T> = Instructions(items.toList())
    }
}

fun interface Instruction<T : GameObject> {
    fun perform(performer: T)
    operator fun invoke(performer: T) = perform(performer)
}
