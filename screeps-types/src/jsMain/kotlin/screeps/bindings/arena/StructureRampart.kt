@file:JsModule("game/prototypes/rampart")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class StructureRampart : OwnedStructure {
    companion object : Prototype<StructureRampart>
}
