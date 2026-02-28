package bot.arena.mode.capturetheflag.ai.pipeline

import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.model.Instructions
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

/**
 * 구조물(특히 타워) 컨트롤 초안.
 *
 * - CTF에서는 타워 운영 템포가 핵심이라, 크립 AI와 분리해서 별도 Executor로 둔다.
 * - "Order/Instruction" (커맨드 패턴)로 반환해 Arena가 일괄 수행하게 한다.
 */
class TowerExecutor {

    fun buildOrders(world: WorldModel): List<Order<*>> {
        val orders = mutableListOf<Order<*>>()

        world.towers
            .filter { it.my == true }
            .forEach { tower ->
                val action = decideTowerAction(tower, world) ?: return@forEach

                val instructions = Instructions<StructureTower> {
                    instruction {
                        action()
                    }
                }
                orders += Order(performer = tower, instructions = instructions)
            }

        return orders
    }

    private fun decideTowerAction(tower: StructureTower, world: WorldModel): (StructureTower.() -> Unit)? {
        val energy = tower.store.getUsedCapacity(RESOURCE_ENERGY.toString()) ?: 0
        if (energy <= 0) return null
        if (tower.cooldown > 0) return null

        // 1) 치유 우선: 가장 피가 낮은 아군(사거리 내)
        val healTarget = lowestHpAllyInRange(tower, world, range = 20)
        if (healTarget != null) {
            return { heal(healTarget) }
        }

        // 2) 공격: 가장 가까운 적(사거리 내)
        val enemy = world.enemyCreeps.minByOrNull { getRange(tower, it) }
        if (enemy != null && getRange(tower, enemy) <= 20) {
            return { attack(enemy) }
        }

        return null
    }

    private fun lowestHpAllyInRange(tower: StructureTower, world: WorldModel, range: Int): Creep? {
        return world.myCreeps
            .filter { it.hits < it.hitsMax }
            .filter { getRange(tower, it) <= range }
            .minByOrNull { it.hits.toDouble() / it.hitsMax.toDouble() }
    }
}
