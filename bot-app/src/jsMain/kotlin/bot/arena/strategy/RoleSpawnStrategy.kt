package bot.arena.strategy

import bot.arena.memory.CreepMemory
import bot.arena.plan.bodyCost
import screeps.bindings.BodyPartConstant
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.getUsedCapacity

/**
 * Role 기반 소환 전략의 공통 추상 클래스.
 *
 * 서브클래스는 [bodyParts]와 [role]만 정의하면
 * - [isDone]: role 카운트 기반 완료 판단
 * - [spawn]:  에너지 체크 → 소환 → role 태깅
 * 이 모두 자동으로 처리된다.
 *
 * ```kotlin
 * class WorkerSpawnStrategy(spawn: StructureSpawn, max: Int)
 *     : RoleSpawnStrategy(spawn, max, CreepMemory.Role.WORKER) {
 *     override val bodyParts = arrayOf(MOVE, CARRY, WORK)
 * }
 * ```
 */
abstract class RoleSpawnStrategy(
    protected val mySpawn: StructureSpawn,
    private val maxCount: Int,
    private val role: String,
) : SpawnStrategy() {

    abstract val bodyParts: Array<out BodyPartConstant>

    private val spawnCost by lazy { bodyCost(bodyParts) }

    override val isDone: Boolean
        get() = CreepMemory.countAlive(role) >= maxCount

    // 이 전략이 직접 시작한 spawn인지 추적.
    // 다른 전략이 spawn 중일 때 creep을 잘못 태깅하는 오염을 방지한다.
    private var isOurSpawn = false

    override fun spawn() {
        val spawning = mySpawn.spawning
        if (spawning != null) {
            // 내가 시작한 spawn이면 태깅 (소환 완료까지 매 틱 덮어씀)
            if (isOurSpawn) CreepMemory.set(spawning.creep, role)
            return
        }
        // 소환이 끝났으면 플래그 초기화
        isOurSpawn = false

        val energy = mySpawn.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0
        if (energy < spawnCost) return

        mySpawn.spawnCreep(bodyParts)
        isOurSpawn = true  // 다음 틱부터 spawning.creep을 태깅
    }

    override fun initialize() {
        super.initialize()
        isOurSpawn = false
    }
}
