package bot.arena.common.plan

import screeps.bindings.BODYPART_COST
import screeps.bindings.BodyPartConstant
import screeps.bindings.CREEP_SPAWN_TIME
import screeps.support.getOrDefault

fun bodyCost(parts: Array<out BodyPartConstant>): Int =
    parts.sumOf { BODYPART_COST.getOrDefault(it, 0) }

fun bodySpawnTime(parts: Array<out BodyPartConstant>): Int =
    parts.size * CREEP_SPAWN_TIME
