package screeps.bindings.arena.ktype

import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.Creep
import screeps.bindings.arena.SpawnCreepResult


data class SpawnCreepResultError(
    override val error: ScreepsReturnCode,
) : SpawnCreepResult {
    override val `object`: Creep?
        get() = null
}
