package bot.arena.mode.capturetheflag.model

data class Order(
    val performerId: String,
    val instructions: List<Instruction<*>>
)
