@file:JsModule("game/prototypes/tower")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.game.Prototype
import screeps.bindings.arena.game.Store

abstract external class StructureTower : OwnedStructure {
    val store: Store
    val cooldown: Int
    fun attack(target: Creep): ScreepsReturnCode
    fun attack(target: Structure): ScreepsReturnCode
    fun heal(target: Creep): ScreepsReturnCode

    companion object : Prototype<StructureTower>
}
