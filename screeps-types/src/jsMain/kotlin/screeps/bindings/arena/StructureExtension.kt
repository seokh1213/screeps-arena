@file:JsModule("game/prototypes/extension")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.arena.OwnedStructure
import screeps.bindings.arena.game.Store

abstract external class StructureExtension : OwnedStructure {
    val store: Store
}

