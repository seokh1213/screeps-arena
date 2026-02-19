@file:JsModule("game/prototypes/spawn")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.BodyPartConstant
import screeps.bindings.DirectionConstant
import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.game.Store

abstract external class StructureSpawn : OwnedStructure {
    val store: Store
    val spawning: Spawning?
    val directions: Array<DirectionConstant>

    fun setDirections(directions: Array<DirectionConstant>): ScreepsReturnCode
    fun spawnCreep(body: Array<out BodyPartConstant>): SpawnCreepResult
}

external interface SpawnCreepResult {
    val `object`: Any?
    val error: ScreepsReturnCode?
}

abstract external class Spawning {
    val needTime: Int
    val remainingTime: Int
    val creep: Creep
    fun cancel(): ScreepsReturnCode?
}
