package app.entry

import bot.arena.mode.Arena
import bot.arena.mode.tutorial.TutorialArena
import screeps.bindings.arena.game.ArenaInfo
import screeps.bindings.arena.utils.installPrettyToStringOverrides

private object Init {
    init {
        println("Arena: " + ArenaInfo.name)
        installPrettyToStringOverrides()
    }
}

val arena: Arena = TutorialArena()

/**
 * Main loop
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    Init
    arena.loop()
}
