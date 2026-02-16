@file:JsModule("game/prototypes/owned-structure")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.arena.Structure

abstract external class OwnedStructure : Structure {
    val my: Boolean?
}

