@file:JsModule("game/prototypes/source")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.arena.GameObject

abstract external class Source : GameObject {
    val energy: Int
    val energyCapacity: Int
}

