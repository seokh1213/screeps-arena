package bot.arena.mode.capturetheflag.ai.core

import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Structure
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

/**
 * RolePolicy = creep 한 개를 어떻게 움직이고 싸울지(미시 규칙).
 *
 * - Squad/Tactic은 "desiredPos / mode / constraints"만 내려준다.
 * - RolePolicy는 환경을 보고 실제 행동(Action)을 고른다.
 */
interface RolePolicy {
    val role: Role
    fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action
}

class RolePolicyRegistry(
    private val policies: Map<Role, RolePolicy>
) {
    fun policyFor(role: Role): RolePolicy? = policies[role]

    companion object {
        fun default(): RolePolicyRegistry = RolePolicyRegistry(
            mapOf(
                Role.WORKER to WorkerPolicy(),
                Role.MELEE to MeleePolicy(),
                Role.RANGER to RangerPolicy(),
                Role.HEALER to HealerPolicy(),
            )
        )
    }
}

private fun Creep.hitPointRatio(): Double = if (hitsMax == 0) 0.0 else hits.toDouble() / hitsMax.toDouble()

private fun shouldRetreatForHealth(creep: Creep, intent: UnitIntent): Boolean {
    val retreatThreshold = intent.constraints.retreatHpRatio ?: return false
    return creep.hitPointRatio() <= retreatThreshold
}

private fun enforceTetherConstraint(creep: Creep, intent: UnitIntent, world: WorldModel): Action.Move? {
    val maximumDistanceToTether = intent.constraints.maxDistanceToTether ?: return null
    val tetherUnitId = intent.tetherToId ?: return null
    val tetherCreep = world.getCreepById(tetherUnitId) ?: return null

    if (getRange(creep, tetherCreep) > maximumDistanceToTether) {
        return Action.Move(tetherCreep.toPosition())
    }

    return null
}

private fun chooseRetreatPosition(creep: Creep, intent: UnitIntent, world: WorldModel): Pos {
    val nearestThreat = world.enemyCreeps.minByOrNull { getRange(creep, it) }
    if (nearestThreat != null) {
        val deltaX = creep.x - nearestThreat.x
        val deltaY = creep.y - nearestThreat.y
        val stepX = when {
            deltaX > 0.0 -> 1
            deltaX < 0.0 -> -1
            else -> 0
        }
        val stepY = when {
            deltaY > 0.0 -> 1
            deltaY < 0.0 -> -1
            else -> 0
        }

        if (stepX != 0 || stepY != 0) {
            return Pos(creep.x.toInt() + stepX, creep.y.toInt() + stepY)
        }
    }

    val tetherCreep = intent.tetherToId?.let(world::getCreepById)
    if (tetherCreep != null) {
        return tetherCreep.toPosition()
    }

    val nearestOwnedFlag = world.flags
        .filter { it.my == true }
        .minByOrNull { getRange(creep, it) }
    if (nearestOwnedFlag != null) {
        return nearestOwnedFlag.toPosition()
    }

    return intent.desiredPos ?: creep.toPosition()
}

private fun chooseClosestEnemy(creep: Creep, world: WorldModel): Creep? =
    world.enemyCreeps.minByOrNull { getRange(creep, it) }

private fun chooseLowestHealthEnemyInRange(creep: Creep, world: WorldModel, rangeLimit: Int): Creep? =
    world.enemyCreeps
        .filter { getRange(creep, it) <= rangeLimit }
        .minByOrNull { it.hitPointRatio() }

private fun moveToDesiredPosition(intent: UnitIntent): Action =
    intent.desiredPos?.let { Action.Move(it) } ?: Action.None

private fun Creep.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun screeps.bindings.arena.Flag.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun Structure.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun StructureTower.needsEnergy(): Boolean =
    (store.getFreeCapacity(RESOURCE_ENERGY.toString()) ?: 0) > 0

private fun StructureContainer.hasEnergy(): Boolean =
    (store.getUsedCapacity(RESOURCE_ENERGY.toString()) ?: 0) > 0

/**
 * 근거리: 점령 유지 + 전열 유지 + 저체력/테더 제약
 */
class MeleePolicy : RolePolicy {
    override val role: Role = Role.MELEE

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        if (shouldRetreatForHealth(creep, intent) || intent.mode == UnitMode.Retreat) {
            return Action.Move(chooseRetreatPosition(creep, intent, world))
        }

        enforceTetherConstraint(creep, intent, world)?.let { return it }

        intent.constraints.mustOccupyPos?.let { occupyPosition ->
            if (getRange(creep, occupyPosition.toHasPosition()) > 0) {
                return Action.Move(occupyPosition)
            }
        }

        val adjacentEnemy = chooseLowestHealthEnemyInRange(creep, world, rangeLimit = 1)
        if (adjacentEnemy != null) {
            return Action.Attack(adjacentEnemy.id.toString())
        }

        if (intent.mode == UnitMode.Hold && intent.desiredPos != null) {
            if (getRange(creep, intent.desiredPos.toHasPosition()) > 1) {
                return Action.Move(intent.desiredPos)
            }
            return Action.None
        }

        return moveToDesiredPosition(intent)
    }
}

/**
 * 레인저: 최대 사거리 유지, 근접 위협 시 카이팅, 저체력 후퇴
 */
class RangerPolicy : RolePolicy {
    override val role: Role = Role.RANGER

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        if (shouldRetreatForHealth(creep, intent) || intent.mode == UnitMode.Retreat) {
            return Action.Move(chooseRetreatPosition(creep, intent, world))
        }

        enforceTetherConstraint(creep, intent, world)?.let { return it }

        val closestEnemy = chooseClosestEnemy(creep, world)
        if (intent.mode == UnitMode.Kite && closestEnemy != null && getRange(creep, closestEnemy) <= 2) {
            return Action.Move(chooseRetreatPosition(creep, intent, world))
        }

        val enemyInRange = chooseLowestHealthEnemyInRange(creep, world, rangeLimit = 3)
        if (enemyInRange != null) {
            return Action.RangedAttack(enemyInRange.id.toString())
        }

        if (intent.mode == UnitMode.Hold && intent.desiredPos != null) {
            if (getRange(creep, intent.desiredPos.toHasPosition()) > 1) {
                return Action.Move(intent.desiredPos)
            }
            return Action.None
        }

        return moveToDesiredPosition(intent)
    }
}

/**
 * 힐러: 아군 치유를 유지하되 근접 위협 시 후퇴
 */
class HealerPolicy : RolePolicy {
    override val role: Role = Role.HEALER

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        if (shouldRetreatForHealth(creep, intent) || intent.mode == UnitMode.Retreat) {
            return Action.Move(chooseRetreatPosition(creep, intent, world))
        }

        enforceTetherConstraint(creep, intent, world)?.let { return it }

        val closestEnemy = chooseClosestEnemy(creep, world)
        if (intent.mode == UnitMode.Kite && closestEnemy != null && getRange(creep, closestEnemy) <= 2) {
            return Action.Move(chooseRetreatPosition(creep, intent, world))
        }

        val injuredAlly = world.myCreeps
            .filter { it.hits < it.hitsMax }
            .filter { getRange(creep, it) <= 3 }
            .minByOrNull { it.hitPointRatio() }

        if (injuredAlly != null) {
            return if (getRange(creep, injuredAlly) <= 1) {
                Action.Heal(injuredAlly.id.toString())
            } else {
                Action.RangedHeal(injuredAlly.id.toString())
            }
        }

        return moveToDesiredPosition(intent)
    }
}

/**
 * 워커: 타워 충전 전 에너지 확인, 부족하면 목표 타워 주변 컨테이너에서 보급
 */
class WorkerPolicy : RolePolicy {
    override val role: Role = Role.WORKER

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        if (shouldRetreatForHealth(creep, intent) || intent.mode == UnitMode.Retreat) {
            return Action.Move(chooseRetreatPosition(creep, intent, world))
        }

        val towerIdToSupport = intent.focusTargetId
        if (towerIdToSupport != null) {
            val targetTower = world.getTowerById(towerIdToSupport)
            val currentEnergy = creep.store.getUsedCapacity(RESOURCE_ENERGY.toString()) ?: 0

            if (currentEnergy <= 0) {
                val supportContainer = chooseEnergyContainerNearTower(creep, targetTower, world)
                if (supportContainer != null) {
                    return if (getRange(creep, supportContainer) <= 1) {
                        Action.Withdraw(supportContainer.id.toString(), RESOURCE_ENERGY)
                    } else {
                        Action.Move(supportContainer.toPosition())
                    }
                }
            }

            if (targetTower != null && targetTower.needsEnergy()) {
                return if (getRange(creep, targetTower) <= 1) {
                    Action.Transfer(targetTower.id.toString(), RESOURCE_ENERGY)
                } else {
                    Action.Move(targetTower.toPosition())
                }
            }
        }

        return moveToDesiredPosition(intent)
    }

    private fun chooseEnergyContainerNearTower(
        workerCreep: Creep,
        targetTower: StructureTower?,
        world: WorldModel,
    ): StructureContainer? {
        val containersWithEnergy = world.containers.filter { it.hasEnergy() }
        if (containersWithEnergy.isEmpty()) return null

        val nearbyContainers = if (targetTower != null) {
            containersWithEnergy.filter { getRange(it, targetTower) <= 8 }
        } else {
            emptyList()
        }

        val candidates = if (nearbyContainers.isNotEmpty()) nearbyContainers else containersWithEnergy
        return candidates.minByOrNull { getRange(workerCreep, it) }
    }
}
