package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.Resource
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getObjectsByPrototype
import screeps.bindings.arena.game.getRange
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

/**
 * Perception + Query 레이어.
 * - raw 오브젝트를 한 번에 수집하고
 * - AI가 반복적으로 쓰는 질의(거리/근처 적/근처 아군 등)를 제공한다.
 */
data class WorldModel(
    val creeps: List<Creep>,
    val flags: List<Flag>,
    val towers: List<StructureTower>,
    val containers: List<StructureContainer>,
    val resources: List<Resource>,
    val bodyParts: List<BodyPart>,
) {
    val myCreeps: List<Creep> = creeps.filter { it.my }
    val enemyCreeps: List<Creep> = creeps.filter { !it.my }

    fun enemiesInRange(pos: screeps.bindings.arena.HasPosition, range: Int): List<Creep> =
        enemyCreeps.filter { getRange(pos, it) <= range }

    fun alliesInRange(pos: screeps.bindings.arena.HasPosition, range: Int): List<Creep> =
        myCreeps.filter { getRange(pos, it) <= range }

    fun range(a: screeps.bindings.arena.HasPosition, b: screeps.bindings.arena.HasPosition): Int = getRange(a, b)

    fun getFlagById(id: String): Flag? = flags.firstOrNull { it.id.toString() == id }
    fun getCreepById(id: String): Creep? = creeps.firstOrNull { it.id.toString() == id }
    fun getTowerById(id: String): StructureTower? = towers.firstOrNull { it.id.toString() == id }

    companion object {
        fun sense(): WorldModel {
            val creeps = getObjectsByPrototype(Creep).toList()
            val flags = getObjectsByPrototype(Flag).toList()
            val towers = getObjectsByPrototype(StructureTower).toList()
            val containers = getObjectsByPrototype(StructureContainer).toList()
            val resources = getObjectsByPrototype(Resource).toList()
            val bodyParts = getObjectsByPrototype(BodyPart).toList()

            return WorldModel(
                creeps = creeps,
                flags = flags,
                towers = towers,
                containers = containers,
                resources = resources,
                bodyParts = bodyParts,
            )
        }
    }
}
