@file:JsModule("game")
@file:JsNonModule
package screeps.bindings.arena.game

@JsName("arenaInfo")
external object ArenaInfo {
    val name: String
    val level: Int
    val season: String
    val ticksLimit: Int
    val cpuTimeLimit: Int
    val cpuTimeLimitFirstTick: Int
}
