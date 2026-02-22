package app.entry

import bot.arena.mode.Arena
import bot.arena.mode.capturetheflag.CaptureTheFlagArena
import screeps.bindings.arena.game.ArenaInfo
import screeps.bindings.arena.utils.pretty

private object Init {
    init {
        println("Arena: ${ArenaInfo.pretty()}")
    }
}

val arena: Arena = CaptureTheFlagArena()

/**
 * Main loop
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    Init
    arena.loop()
}
