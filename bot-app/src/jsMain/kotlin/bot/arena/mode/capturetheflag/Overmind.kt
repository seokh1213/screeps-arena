package bot.arena.mode.capturetheflag

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.RolePolicyRegistry
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.pipeline.GroupCoordinator
import bot.arena.mode.capturetheflag.ai.pipeline.SquadExecutor
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector
import bot.arena.mode.capturetheflag.ai.pipeline.TowerExecutor
import bot.arena.mode.capturetheflag.ai.presets.CaptureTheFlagDirectors
import bot.arena.mode.capturetheflag.memory.Memory
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.CreepContext
import bot.arena.mode.capturetheflag.model.Order
import bot.arena.mode.capturetheflag.model.Phase
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.ATTACK
import screeps.bindings.CARRY
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.game.getObjectsByPrototype
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

class Overmind {
    private val memory = Memory()

    // 공유 상태(Director/Coordinator/Executor 공통)
    private val blackboard = Blackboard()
    private val groups = GroupStore()

    // 파이프라인 3요소
    private val coordinator = GroupCoordinator(groups)
    private val executor = SquadExecutor(
        policies = RolePolicyRegistry.default(),
        roleOf = ::roleOf,
    )
    private val towerExecutor = TowerExecutor()

    // Director는 Utility/Hierarchical Task Network 중 선택 가능
    private val directorMode: DirectorMode = DirectorMode.UTILITY
    private val director: StrategyDirector by lazy {
        when (directorMode) {
            DirectorMode.UTILITY -> CaptureTheFlagDirectors.utility(roleOf = ::roleOf)
            DirectorMode.HIERARCHICAL_TASK_NETWORK -> CaptureTheFlagDirectors.hierarchicalTaskNetwork()
        }
    }

    init {
        updateMemory()
        assignCreepRole(memory.currentContext)
    }

    fun commandOrder(): List<Order<*>> {
        updateMemory()

        // Perception
        val world = WorldModel.sense()

        // 역할 메모리 유지(사망 creep 제거)
        memory.creepMemory.retainAll(world.myCreeps)
        assignCreepRole(memory.currentContext)

        // 1) Director: 전략 계획
        val plan = director.tick(world, blackboard, groups)

        // 2) Coordinator: 그룹 반영
        val squads = coordinator.apply(plan, world, blackboard)

        // 3) Executor: orders 생성
        val creepOrders = executor.execute(squads, world, blackboard)
        val towerOrders = towerExecutor.buildOrders(world)
        return creepOrders + towerOrders
    }

    private fun updateMemory() {
        val context = getCurrentContext()
        val phase = evaluatePhase(context)

        memory.updateState(phase, context)
    }

    private fun getCurrentContext() = Context(
        creeps = getObjectsByPrototype(Creep),
        flags = getObjectsByPrototype(Flag),
        bodyPartItems = getObjectsByPrototype(BodyPart)
    )

    /**
     * 두뇌 역할 - 현재와 이전의 상태를 보고 판단
     */
    private fun evaluatePhase(currentContext: Context): Phase {
        val (beforePhase, beforeContext) = memory.beforeState

        return Phase.INITIAL
    }

    private fun roleOf(creep: Creep): Role {
        return memory.creepMemory[creep]?.role ?: creep.determineRole()
    }

    private fun assignCreepRole(context: Context) {
        val myCreeps = context.myCreeps

        myCreeps.forEach { creep ->
            val creepContext = memory.creepMemory[creep]
            if (creepContext?.id?.isNotEmpty() == true) {
                return@forEach
            }

            memory.creepMemory[creep] = CreepContext(
                id = creep.id,
                role = creep.determineRole()
            )
        }
    }

    private fun Creep.determineRole(): Role {
        val bodyParts = body.map { it.type }
        return when {
            bodyParts.contains(CARRY) -> Role.WORKER
            bodyParts.contains(HEAL) -> Role.HEALER
            bodyParts.contains(RANGED_ATTACK) -> Role.RANGER
            bodyParts.contains(ATTACK) -> Role.MELEE
            else -> Role.CREEP
        }
    }

    private enum class DirectorMode {
        UTILITY,
        HIERARCHICAL_TASK_NETWORK,
    }
}
