package bot.arena.mode.capturetheflag.ai.pipeline

import bot.arena.mode.capturetheflag.ai.core.Action
import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.RolePolicyRegistry
import bot.arena.mode.capturetheflag.ai.core.SquadView
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.model.Instructions
import bot.arena.mode.capturetheflag.model.Order
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Structure

/**
 * SquadExecutor = (스쿼드 전술 -> 유닛 intent) + (RolePolicy -> Action) + (Order/Instruction 실행)
 */
class SquadExecutor(
    private val policies: RolePolicyRegistry,
    private val roleOf: (Creep) -> Role,
) {
    fun execute(squads: List<SquadView>, world: WorldModel, blackboard: Blackboard): List<Order<*>> {
        val orders = mutableListOf<Order<*>>()

        // 1) 스쿼드 전술로 intent 생성
        val intentsByCreep = mutableMapOf<String, UnitIntent>()
        squads.forEach { squad ->
            val tactic = squad.tactic ?: return@forEach
            tactic.tick(
                squadId = squad.id,
                members = squad.members,
                world = world,
                blackboard = blackboard,
                tacticContext = squad.tacticContext,
                outputIntents = intentsByCreep,
            )
        }

        // 2) 유닛별 Action 결정 + Order 변환
        squads.flatMap { it.members }.distinctBy { it.id.toString() }.forEach { creep ->
            val intent = intentsByCreep[creep.id.toString()] ?: return@forEach
            val role = roleOf(creep)
            val policy = policies.policyFor(role) ?: return@forEach
            val action = policy.decide(creep, intent, world, blackboard)
            orders += action.toOrder(creep, world)
        }

        return orders
    }

    private fun Action.toOrder(creep: Creep, world: WorldModel): Order<*> {
        val instructions = Instructions<Creep> {
            instruction {
                when (this@toOrder) {
                    is Action.None -> Unit
                    is Action.Move -> moveTo(to.toHasPosition())
                    is Action.Attack -> {
                        val target = world.getCreepById(targetId)
                        if (target != null) attack(target)
                    }
                    is Action.RangedAttack -> {
                        val target = world.getCreepById(targetId)
                        if (target != null) rangedAttack(target)
                    }
                    is Action.Heal -> {
                        val target = world.getCreepById(targetId)
                        if (target != null) heal(target)
                    }
                    is Action.RangedHeal -> {
                        val target = world.getCreepById(targetId)
                        if (target != null) rangedHeal(target)
                    }
                    is Action.Transfer -> {
                        val target = (world.getTowerById(targetId) ?: world.containers.firstOrNull { it.id.toString() == targetId }) as? Structure
                        if (target != null) transfer(target, resource)
                    }
                    is Action.Withdraw -> {
                        val target = (world.containers.firstOrNull { it.id.toString() == targetId }) as? Structure
                        if (target != null) withdraw(target, resource)
                    }
                }
            }
        }

        return Order(performer = creep, instructions = instructions)
    }
}
