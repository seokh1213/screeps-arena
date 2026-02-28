@file:JsModule("game/prototypes/structure")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class Structure : GameObject {
    val hits: Int?
    val hitsMax: Int?

    companion object : Prototype<Structure>
}
