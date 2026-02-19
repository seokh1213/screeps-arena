@file:JsModule("game/prototypes/creep")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.BodyPartConstant
import screeps.bindings.DirectionConstant
import screeps.bindings.ResourceConstant
import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.game.FindPathOptions
import screeps.bindings.arena.game.Store

abstract external class Creep : GameObject {
    val body: Array<BodyPart>
    val fatigue: Int
    val hits: Int
    val hitsMax: Int
    val my: Boolean
    val store: Store
    val spawning: Boolean

    fun attack(target: Creep): ScreepsReturnCode
    fun attack(target: Structure): ScreepsReturnCode
    fun attack(target: ConstructionSite): ScreepsReturnCode
    fun build(target: ConstructionSite): ScreepsReturnCode
    fun drop(resource: ResourceConstant, amount: Int = definedExternally): ScreepsReturnCode
    fun harvest(target: Source): ScreepsReturnCode
    fun heal(target: Creep): ScreepsReturnCode
    fun move(direction: DirectionConstant): ScreepsReturnCode
    fun moveTo(target: HasPosition, options: FindPathOptions = definedExternally): ScreepsReturnCode
    fun pickup(target: Resource): ScreepsReturnCode
    fun pull(target: Creep): ScreepsReturnCode
    fun rangedAttack(target: Creep): ScreepsReturnCode
    fun rangedAttack(target: Structure): ScreepsReturnCode
    fun rangedHeal(target: Creep): ScreepsReturnCode
    fun rangedMassAttack(): ScreepsReturnCode
    fun transfer(target: Structure, resource: ResourceConstant, amount: Int = definedExternally): ScreepsReturnCode
    fun transfer(target: Creep, resource: ResourceConstant, amount: Int = definedExternally): ScreepsReturnCode
    fun withdraw(target: Structure, resource: ResourceConstant, amount: Int = definedExternally): ScreepsReturnCode
}

external interface BodyPart {
    val type: BodyPartConstant
    val hits: Int
}

