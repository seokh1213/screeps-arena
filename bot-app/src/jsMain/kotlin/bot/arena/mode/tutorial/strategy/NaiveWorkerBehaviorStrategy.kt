package bot.arena.mode.tutorial.strategy

import bot.arena.memory.CreepMemory
import bot.arena.memory.CreepRoles
import bot.arena.strategy.BehaviorStrategy
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.Source
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.arena.game.getObjectsByPrototype
import kotlin.math.abs

/**
 * WORKER role을 가진 creep들의 행동 전략: source 수집 → spawn 전달 무한반복.
 *
 * - [CreepMemory.Role.WORKER] 태그가 있는 내 creep만 대상으로 한다.
 * - [isDone]은 항상 false (무한 전략).
 */
class NaiveWorkerBehaviorStrategy(
    private val mySpawn: StructureSpawn,
) : BehaviorStrategy() {

    private enum class WorkerRole { HARVESTING, DELIVERING }

    private val workerRoles = mutableMapOf<String, WorkerRole>()

    override fun behave() {
        val myWorkers = getObjectsByPrototype(Creep)
            .filter { it.my && !it.spawning && CreepMemory.get(it) == CreepRoles.WORKER }
        val sources = getObjectsByPrototype(Source)

        myWorkers.forEach { creep ->
            val role = workerRoles.getOrPut(creep.id.toString()) { WorkerRole.HARVESTING }
            when (role) {
                WorkerRole.HARVESTING -> harvest(creep, sources)
                WorkerRole.DELIVERING -> deliver(creep)
            }
        }
    }

    private fun harvest(creep: Creep, sources: Array<Source>) {
        if ((creep.store.getFreeCapacity(RESOURCE_ENERGY.unsafeCast<String?>()) ?: 0) == 0) {
            workerRoles[creep.id.toString()] = WorkerRole.DELIVERING
            deliver(creep)
            return
        }
        val source = sources.minByOrNull { dist(creep, it) } ?: return
        if (dist(creep, source) <= 1) creep.harvest(source) else creep.moveTo(source)
    }

    private fun deliver(creep: Creep) {
        if ((creep.store.getUsedCapacity(RESOURCE_ENERGY.unsafeCast<String?>()) ?: 0) == 0) {
            workerRoles[creep.id.toString()] = WorkerRole.HARVESTING
            return
        }
        if (dist(creep, mySpawn) <= 1) creep.transfer(mySpawn, RESOURCE_ENERGY)
        else creep.moveTo(mySpawn)
    }

    private fun dist(a: HasPosition, b: HasPosition): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return maxOf(abs(dx), abs(dy))
    }
}
