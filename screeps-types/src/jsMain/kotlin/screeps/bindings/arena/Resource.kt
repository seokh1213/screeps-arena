@file:JsModule("game/prototypes/resource")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.ResourceConstant
import screeps.bindings.arena.GameObject

abstract external class Resource : GameObject {
    val amount: Int
    val resourceType: String // ResourceType = string
}

