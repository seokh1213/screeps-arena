@file:JsModule("game/prototypes/source")
@file:JsNonModule

package screeps.bindings.arena

abstract external class Source : GameObject {
    val energy: Int
    val energyCapacity: Int
}

