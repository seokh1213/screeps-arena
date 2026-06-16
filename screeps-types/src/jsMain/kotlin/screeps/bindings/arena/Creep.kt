@file:JsModule("game/prototypes/creep")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.BodyPartConstant
import screeps.bindings.DirectionConstant
import screeps.bindings.ResourceConstant
import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.game.FindPathOptions
import screeps.bindings.arena.game.Prototype
import screeps.bindings.arena.game.Store

typealias CreepAttackResult = ScreepsReturnCode
typealias CreepBuildResult = ScreepsReturnCode
typealias CreepDropResult = ScreepsReturnCode
typealias CreepHarvestResult = ScreepsReturnCode
typealias CreepHealResult = ScreepsReturnCode
typealias CreepMoveResult = ScreepsReturnCode
typealias CreepPickupResult = ScreepsReturnCode
typealias CreepPullResult = ScreepsReturnCode
typealias CreepRangedAttackResult = ScreepsReturnCode
typealias CreepRangedHealResult = ScreepsReturnCode
typealias CreepRangedMassAttackResult = ScreepsReturnCode
typealias CreepTransferResult = ScreepsReturnCode
typealias CreepWithdrawResult = ScreepsReturnCode

abstract external class Creep : GameObject {
    val body: Array<BodyPart>
    val fatigue: Int
    val hits: Int
    val hitsMax: Int
    val my: Boolean
    val store: Store
    val spawning: Boolean

    fun attack(target: Creep): CreepAttackResult
    fun attack(target: Structure): CreepAttackResult
    fun attack(target: ConstructionSite): CreepAttackResult
    fun build(target: ConstructionSite): CreepBuildResult
    fun drop(resource: ResourceConstant, amount: Int = definedExternally): CreepDropResult
    fun harvest(target: Source): CreepHarvestResult
    fun heal(target: Creep): CreepHealResult
    fun move(direction: DirectionConstant): CreepMoveResult
    fun moveTo(target: HasPosition, options: FindPathOptions = definedExternally): CreepMoveResult
    fun pickup(target: Resource): CreepPickupResult
    fun pull(target: Creep): CreepPullResult
    fun rangedAttack(target: Creep): CreepRangedAttackResult
    fun rangedAttack(target: Structure): CreepRangedAttackResult
    fun rangedHeal(target: Creep): CreepRangedHealResult
    fun rangedMassAttack(): CreepRangedMassAttackResult
    fun transfer(target: Structure, resource: ResourceConstant, amount: Int = definedExternally): CreepTransferResult
    fun transfer(target: Creep, resource: ResourceConstant, amount: Int = definedExternally): CreepTransferResult
    fun withdraw(target: Structure, resource: ResourceConstant, amount: Int = definedExternally): CreepWithdrawResult

    companion object : Prototype<Creep>
}

typealias BodyPartType = BodyPartConstant

external interface BodyPart {
    val type: BodyPartConstant
    val hits: Int
}
