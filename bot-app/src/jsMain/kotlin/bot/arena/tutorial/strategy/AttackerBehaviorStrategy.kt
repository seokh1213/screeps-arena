package bot.arena.tutorial.strategy

import bot.arena.memory.CreepMemory
import bot.arena.memory.CreepRoles
import bot.arena.strategy.BehaviorStrategy
import screeps.bindings.arena.Creep
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.arena.game.PrototypeCreep
import screeps.bindings.arena.game.getObjectsByPrototype
import kotlin.math.abs

/**
 * ATTACKER role을 가진 creep들의 수비 전략.
 *
 * - [mySpawn]을 중심으로 [guardRange] 이내에 머무르며 적을 공격한다.
 * - 적이 guardRange 이내로 들어오면 rangedAttack.
 * - 적이 없으면 spawn 근처로 귀환.
 * - spawn 밖으로 나가지 않는다.
 */
class AttackerBehaviorStrategy(
    private val mySpawn: StructureSpawn,
    private val guardRange: Int = 5,
) : BehaviorStrategy() {

    override fun behave() {
        val myAttackers = getObjectsByPrototype(PrototypeCreep)
            .filter { it.my && !it.spawning && CreepMemory.get(it) == CreepRoles.ATTACKER }

        if (myAttackers.isEmpty()) return

        val enemyCreeps = getObjectsByPrototype(PrototypeCreep).filter { !it.my }

        myAttackers.forEach { attacker ->
            defend(attacker, enemyCreeps)
        }
    }

    private fun defend(attacker: Creep, enemyCreeps: List<Creep>) {
        // guardRange 이내의 적만 대상으로 함
        val nearbyEnemies = enemyCreeps.filter { dist(attacker, it) <= guardRange }

        if (nearbyEnemies.isNotEmpty()) {
            // 가장 가까운 적 공격
            val target = nearbyEnemies.minByOrNull { dist(attacker, it) }!!
            if (dist(attacker, target) <= 3) {
                attacker.rangedAttack(target)
            } else {
                // 사거리 안으로 이동 (단, spawn 범위 밖으로는 안 나감)
                if (dist(attacker, mySpawn) < guardRange) {
                    attacker.moveTo(target)
                }
            }
        } else {
            // 적 없으면 spawn 근처로 귀환
            if (dist(attacker, mySpawn) > 3) {
                attacker.moveTo(mySpawn)
            }
        }
    }

    private fun dist(a: HasPosition, b: HasPosition): Double {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return maxOf(abs(dx), abs(dy))
    }
}
