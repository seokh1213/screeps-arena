@file:JsModule("game/prototypes/container")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.arena.OwnedStructure
import screeps.bindings.arena.game.Store

abstract external class StructureContainer : OwnedStructure {
    val store: Store
}

