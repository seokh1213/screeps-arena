package bot.arena.mode.capturetheflag.behavior.actions

import bot.arena.mode.capturetheflag.model.Instructions
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower

object CreepActions {

    fun moveTo(creep: Creep, target: HasPosition): Order<Creep> =
        Order(creep, Instructions {
            instruction { moveTo(target) }
        })

    fun attackTarget(creep: Creep, target: Creep): Order<Creep> =
        Order(creep, Instructions {
            instruction { attack(target); moveTo(target) }
        })

    fun rangedAttackTarget(creep: Creep, target: Creep): Order<Creep> =
        Order(creep, Instructions {
            instruction { rangedAttack(target) }
        })

    fun rangedAttackAndMove(creep: Creep, target: Creep, moveTarget: HasPosition): Order<Creep> =
        Order(creep, Instructions {
            instruction { rangedAttack(target); moveTo(moveTarget) }
        })

    fun healAlly(creep: Creep, target: Creep): Order<Creep> =
        Order(creep, Instructions {
            instruction {
                if (getRangeTo(target) <= 1) heal(target) else rangedHeal(target)
            }
        })

    fun healAndMove(creep: Creep, target: Creep, moveTarget: HasPosition): Order<Creep> =
        Order(creep, Instructions {
            instruction {
                if (getRangeTo(target) <= 1) heal(target) else rangedHeal(target)
                moveTo(moveTarget)
            }
        })

    fun chargeTower(creep: Creep, tower: StructureTower): Order<Creep> =
        Order(creep, Instructions {
            instruction {
                if (getRangeTo(tower) <= 1) transfer(tower, RESOURCE_ENERGY)
                else moveTo(tower)
            }
        })

    fun withdrawEnergy(creep: Creep, container: StructureContainer): Order<Creep> =
        Order(creep, Instructions {
            instruction {
                if (getRangeTo(container) <= 1) withdraw(container, RESOURCE_ENERGY)
                else moveTo(container)
            }
        })

    fun captureFlag(creep: Creep, flag: HasPosition): Order<Creep> =
        Order(creep, Instructions {
            instruction { moveTo(flag) }
        })

    fun retreat(creep: Creep, awayFrom: HasPosition): Order<Creep> =
        Order(creep, Instructions {
            instruction {
                val path = findPathTo(awayFrom, js("({flee: true})").unsafeCast<screeps.bindings.arena.game.FindPathOptions>())
                if (path.isNotEmpty()) moveTo(path[0])
            }
        })
}
