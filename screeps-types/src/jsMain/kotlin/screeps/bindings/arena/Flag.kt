@file:JsModule("game/prototypes/flag")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.arena.GameObject

abstract external class Flag : GameObject {
    val my: Boolean?
}

