package bot.arena.mode.capturetheflag.behavior.conditions

import bot.arena.mode.capturetheflag.model.Context
import screeps.bindings.arena.Creep
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

private const val ENERGY = "energy"

object Conditions {

    fun hasEnergy(creep: Creep): Boolean =
        (creep.store.getUsedCapacity(ENERGY) ?: 0) > 0

    fun isEnergyFull(creep: Creep): Boolean =
        (creep.store.getFreeCapacity(ENERGY) ?: 0) == 0

    fun hpRatio(creep: Creep): Double =
        creep.hits.toDouble() / creep.hitsMax

    fun hpBelow(creep: Creep, ratio: Double): Boolean =
        hpRatio(creep) < ratio

    fun enemiesInRange(pos: HasPosition, range: Int, ctx: Context): List<Creep> =
        ctx.enemyCreeps.filter { getRange(pos, it) <= range }

    fun distanceTo(a: HasPosition, b: HasPosition): Int = getRange(a, b)

    fun towerNeedsEnergy(tower: StructureTower): Boolean =
        (tower.store.getFreeCapacity(ENERGY) ?: 0) > 0

    fun nearestContainerWithEnergy(pos: HasPosition, ctx: Context): StructureContainer? =
        ctx.containers
            .filter { (it.store.getUsedCapacity(ENERGY) ?: 0) > 0 }
            .minByOrNull { getRange(pos, it) }
}
