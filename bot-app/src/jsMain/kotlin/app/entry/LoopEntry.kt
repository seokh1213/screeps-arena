package app.entry

import bot.arena.mode.Arena
import bot.arena.mode.capturetheflag.CaptureTheFlagArena

val arena: Arena = CaptureTheFlagArena()

/**
 * Main loop
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
fun loop() {
    arena.loop()
}
