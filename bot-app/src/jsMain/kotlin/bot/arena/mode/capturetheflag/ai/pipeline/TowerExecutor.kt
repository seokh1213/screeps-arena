package bot.arena.mode.capturetheflag.ai.pipeline

import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.core.controlledById
import bot.arena.mode.capturetheflag.model.Instructions
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

/**
 * 타워 전용 실행기.
 *
 * - 방어/지원 안정성을 위해 크립 실행기와 분리
 * - 깃발 기반 운영: 제어 중인 깃발 주변 위협과 저체력 적을 우선 처리
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
        val availableEnergy = tower.store.getUsedCapacity(RESOURCE_ENERGY.toString()) ?: 0
        if (availableEnergy <= 0) return null
        if (tower.cooldown > 0) return null

        val injuredAlly = chooseMostInjuredAllyInRange(tower, world, range = 20)
        if (injuredAlly != null && injuredAlly.hitPointRatio() <= 0.6) {
            return { heal(injuredAlly) }
        }

        val prioritizedEnemy = choosePrioritizedEnemyTarget(tower, world)
        if (prioritizedEnemy != null) {
            return { attack(prioritizedEnemy) }
        }

        if (injuredAlly != null) {
            return { heal(injuredAlly) }
        }

        return null
    }

    private fun choosePrioritizedEnemyTarget(tower: StructureTower, world: WorldModel): Creep? {
        val controllingFlag = tower.controlledById?.let(world::getFlagById)

        return world.enemyCreeps
            .filter { getRange(tower, it) <= 20 }
            .maxByOrNull { enemyCreep ->
                val lowHealthBonus = (1.0 - enemyCreep.hitPointRatio()) * 100.0
                val distanceFromTower = getRange(tower, enemyCreep)
                val localThreatBonus = (20 - distanceFromTower).coerceAtLeast(0) * 1.5

                val flagProximityBonus = if (controllingFlag != null) {
                    val distanceFromFlag = getRange(controllingFlag, enemyCreep)
                    (12 - distanceFromFlag).coerceAtLeast(0) * 3.0
                } else {
                    0.0
                }

                lowHealthBonus + localThreatBonus + flagProximityBonus
            }
    }

    private fun chooseMostInjuredAllyInRange(tower: StructureTower, world: WorldModel, range: Int): Creep? {
        return world.myCreeps
            .filter { it.hits < it.hitsMax }
            .filter { getRange(tower, it) <= range }
            .minByOrNull { it.hitPointRatio() }
    }
}

private fun Creep.hitPointRatio(): Double =
    if (hitsMax == 0) 0.0 else hits.toDouble() / hitsMax.toDouble()
