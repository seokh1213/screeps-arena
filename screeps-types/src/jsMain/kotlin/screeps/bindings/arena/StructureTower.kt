@file:JsModule("game/prototypes/tower")
@file:JsNonModule
package screeps.bindings.arena

import screeps.bindings.*
import screeps.bindings.arena.Creep
import screeps.bindings.arena.OwnedStructure
import screeps.bindings.arena.game.Store
import screeps.bindings.arena.Structure

abstract external class StructureTower : OwnedStructure {
    val store: Store
    val cooldown: Int
    fun attack(target: Creep): ScreepsReturnCode
    fun attack(target: Structure): ScreepsReturnCode
    fun heal(target: Creep): ScreepsReturnCode
}

