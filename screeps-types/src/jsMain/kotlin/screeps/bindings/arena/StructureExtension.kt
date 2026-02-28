@file:JsModule("game/prototypes/extension")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype
import screeps.bindings.arena.game.Store

abstract external class StructureExtension : OwnedStructure {
    val store: Store

    companion object : Prototype<StructureExtension>
}
