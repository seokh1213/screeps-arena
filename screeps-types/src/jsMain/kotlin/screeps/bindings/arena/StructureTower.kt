@file:JsModule("game/prototypes/tower")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.game.Prototype
import screeps.bindings.arena.game.Store

typealias TowerAttackResult = ScreepsReturnCode
typealias TowerHealResult = ScreepsReturnCode

abstract external class StructureTower : OwnedStructure {
    val store: Store
    val cooldown: Int
    fun attack(target: Creep): TowerAttackResult
    fun attack(target: Structure): TowerAttackResult
    fun heal(target: Creep): TowerHealResult

    companion object : Prototype<StructureTower>
}
