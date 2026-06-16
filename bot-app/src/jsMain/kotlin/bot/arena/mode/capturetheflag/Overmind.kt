package bot.arena.mode.capturetheflag

import bot.arena.mode.capturetheflag.behavior.SquadBehaviorRunner
import bot.arena.mode.capturetheflag.model.BattlePlan
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.Order
import bot.arena.mode.capturetheflag.model.StrategyDirector
import bot.arena.mode.capturetheflag.strategy.CTFStrategyDirector
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getObjectsByPrototype
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart

class Overmind {
    private val director: StrategyDirector = CTFStrategyDirector()
    private val runners = mutableMapOf<String, SquadBehaviorRunner>()

    fun commandOrder(): List<Order<*>> {
        val ctx = getCurrentContext()
        return makeOrders(ctx)
    }

    private fun getCurrentContext() = Context(
        creeps = getObjectsByPrototype(Creep).toList(),
        flags = getObjectsByPrototype(Flag).toList(),
        bodyPartItems = getObjectsByPrototype(BodyPart).toList(),
        towers = getObjectsByPrototype(StructureTower).toList(),
        containers = getObjectsByPrototype(StructureContainer).toList(),
    )

    private fun makeOrders(ctx: Context): List<Order<*>> {
        val battlePlan = director.plan(ctx)
        ensureRunners(battlePlan, ctx)
        return executeRunners(battlePlan, ctx)
    }

    private fun ensureRunners(plan: BattlePlan, ctx: Context) {
        plan.allSquads.forEach { squad ->
            runners.getOrPut(squad.squadId) {
                val initialState = squad.behaviorFactory.createInitialState(ctx, squad.creeps)
                SquadBehaviorRunner(squad.squadId, initialState)
            }
        }

        // 해체된 스쿼드의 러너 제거
        val activeSquadIds = plan.allSquads.mapTo(mutableSetOf()) { it.squadId }
        runners.keys.retainAll(activeSquadIds)
    }

    private fun executeRunners(plan: BattlePlan, ctx: Context): List<Order<Creep>> =
        plan.allSquads.flatMap { squad ->
            val runner = runners[squad.squadId] ?: return@flatMap emptyList()
            val livingCreeps = squad.creeps.filter { it.exists }
            runner.tick(livingCreeps, ctx)
        }
}
