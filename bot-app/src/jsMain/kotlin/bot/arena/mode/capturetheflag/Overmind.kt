package bot.arena.mode.capturetheflag

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.RolePolicyRegistry
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.core.determineRole
import bot.arena.mode.capturetheflag.ai.pipeline.GroupCoordinator
import bot.arena.mode.capturetheflag.ai.pipeline.SquadExecutor
import bot.arena.mode.capturetheflag.ai.pipeline.StrategyDirector
import bot.arena.mode.capturetheflag.ai.pipeline.TowerExecutor
import bot.arena.mode.capturetheflag.ai.presets.CaptureTheFlagDirectors
import bot.arena.mode.capturetheflag.memory.Memory
import bot.arena.mode.capturetheflag.model.CreepContext
import bot.arena.mode.capturetheflag.model.Order
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.arena.Creep

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

    private val director: StrategyDirector =
        CaptureTheFlagDirectors.hybridUtilitySelectingHierarchicalTaskNetworkPlans()

    init {
        val world = WorldModel.sense()
        memory.creepMemory.retainAll(world.myCreeps)
        assignCreepRole(world.myCreeps)
    }

    fun commandOrder(): List<Order<*>> {
        val world = WorldModel.sense()

        // 역할 메모리 유지(사망 creep 제거)
        memory.creepMemory.retainAll(world.myCreeps)
        assignCreepRole(world.myCreeps)

        // 1) Director: 전략 계획
        val plan = director.tick(world, blackboard, groups)

        // 2) Coordinator: 그룹 반영
        val squads = coordinator.apply(plan, world)

        // 3) Executor: orders 생성
        val creepOrders = executor.execute(squads, world, blackboard)
        val towerOrders = towerExecutor.buildOrders(world)
        return creepOrders + towerOrders
    }

    private fun roleOf(creep: Creep): Role {
        return memory.creepMemory[creep]?.role ?: creep.determineRole()
    }

    private fun assignCreepRole(myCreeps: List<Creep>) {
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
}
