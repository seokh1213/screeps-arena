@file:JsModule("game/prototypes/resource")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class Resource : GameObject {
    val amount: Int
    val resourceType: String // ResourceType = string

    companion object : Prototype<Resource>
}
