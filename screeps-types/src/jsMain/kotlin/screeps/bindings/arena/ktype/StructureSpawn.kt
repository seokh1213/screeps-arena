package screeps.bindings.arena.ktype

import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.SpawnCreepResult


data class SpawnCreepResultError(
    override val error: ScreepsReturnCode,
) : SpawnCreepResult {
    override val `object`: Any?
        get() = null
}