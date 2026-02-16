@file:JsModule("game/prototypes")
@file:JsNonModule
package screeps.bindings.arena.capturetheflag.basic

import screeps.bindings.arena.GameObject
import screeps.bindings.arena.game.Prototype

@JsName("Flag")
abstract external class Flag : GameObject {
    val my: Boolean

    companion object : Prototype<Flag>
}
