@file:JsModule("game/prototypes/resource")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

typealias ResourceType = String

abstract external class Resource : GameObject {
    val amount: Int
    val resourceType: ResourceType

    companion object : Prototype<Resource>
}
