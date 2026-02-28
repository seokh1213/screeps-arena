@file:JsModule("game/prototypes/source")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class Source : GameObject {
    val energy: Int
    val energyCapacity: Int

    companion object : Prototype<Source>
}
