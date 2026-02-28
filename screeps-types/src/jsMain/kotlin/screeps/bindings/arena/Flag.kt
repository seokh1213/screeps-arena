@file:JsModule("game/prototypes/flag")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class Flag : GameObject {
    val my: Boolean?

    companion object : Prototype<Flag>
}

