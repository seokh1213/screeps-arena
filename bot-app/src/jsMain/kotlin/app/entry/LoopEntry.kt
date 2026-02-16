package app.entry

import bot.arena.tutorial.TutorialArena
import screeps.bindings.arena.game.ArenaInfo

private object Init {
    init {
        println("Arena: " + ArenaInfo.name)
    }
}

val arena = TutorialArena()

/**
 * Main loop
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    Init
    arena.loop()
}
