@file:JsModule("game/prototypes/resource")
@file:JsNonModule

package screeps.bindings.arena

abstract external class Resource : GameObject {
    val amount: Int
    val resourceType: String // ResourceType = string
}

