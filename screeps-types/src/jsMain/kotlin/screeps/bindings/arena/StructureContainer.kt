@file:JsModule("game/prototypes/container")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype
import screeps.bindings.arena.game.Store

abstract external class StructureContainer : OwnedStructure {
    val store: Store

    companion object : Prototype<StructureContainer>
}
