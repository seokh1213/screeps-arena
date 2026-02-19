@file:JsModule("game/prototypes/structure")
@file:JsNonModule

package screeps.bindings.arena

abstract external class Structure : GameObject {
    val hits: Int?
    val hitsMax: Int?
}

