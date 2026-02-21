package bot.arena.tutorial.strategy

import bot.arena.memory.CreepRoles
import bot.arena.strategy.RoleSpawnStrategy
import screeps.bindings.BodyPartConstant
import screeps.bindings.MOVE
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.TOUGH
import screeps.bindings.arena.StructureSpawn

/**
 * Attacker(TOUGH×3, RANGED_ATTACK, MOVE×2)를 [maxAttackers]마리까지 소환한다.
 * 소환 후 ATTACKER role 태깅은 [RoleSpawnStrategy]가 자동 처리.
 */
class AttackerSpawnStrategy(
    mySpawn: StructureSpawn,
    maxAttackers: Int,
) : RoleSpawnStrategy(mySpawn, maxAttackers, CreepRoles.ATTACKER) {

    override val bodyParts: Array<out BodyPartConstant> = arrayOf(TOUGH, TOUGH, TOUGH, RANGED_ATTACK, MOVE, MOVE)
}
