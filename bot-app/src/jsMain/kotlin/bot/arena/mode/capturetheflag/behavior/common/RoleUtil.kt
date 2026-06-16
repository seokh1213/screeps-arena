package bot.arena.mode.capturetheflag.behavior.common

import screeps.bindings.ATTACK
import screeps.bindings.CARRY
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.arena.Creep

fun Creep.isMelee(): Boolean = body.any { it.type == ATTACK }
fun Creep.isRanger(): Boolean = body.any { it.type == RANGED_ATTACK }
fun Creep.isHealer(): Boolean = body.any { it.type == HEAL }
fun Creep.isWorker(): Boolean = body.any { it.type == CARRY }
