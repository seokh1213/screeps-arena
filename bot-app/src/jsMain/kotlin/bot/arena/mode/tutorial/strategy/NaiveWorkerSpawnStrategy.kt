package bot.arena.mode.tutorial.strategy

import bot.arena.common.memory.CreepMemory
import screeps.bindings.BodyPartConstant
import screeps.bindings.CARRY
import screeps.bindings.MOVE
import screeps.bindings.WORK
import screeps.bindings.arena.StructureSpawn

/**
 * naive worker(WORK, CARRY, MOVE)를 [maxWorkers]마리까지 소환한다.
 * 소환 후 WORKER role 태깅은 [RoleSpawnStrategy]가 자동 처리.
 */
class NaiveWorkerSpawnStrategy(
    mySpawn: StructureSpawn,
    maxWorkers: Int = 3,
    creepMemory: CreepMemory<String>,
) : RoleSpawnStrategy(mySpawn, maxWorkers, CreepRoles.WORKER, creepMemory) {

    override val bodyParts: Array<out BodyPartConstant> = arrayOf(MOVE, CARRY, WORK)
}
