package bot.arena.mode.capturetheflag.ai.core

import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Structure
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

private fun Creep.hpRatio(): Double = if (hitsMax == 0) 0.0 else hits.toDouble() / hitsMax.toDouble()

/**
 * 근거리: (1) 강제 점령 위치(mustOccupyPos)가 있으면 그곳을 최우선
 * (2) 인접 적이 있으면 공격
 * (3) 그 외는 desiredPos로 이동
 */
class MeleePolicy : RolePolicy {
    override val role: Role = Role.MELEE

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        // retreat 규칙(공통)
        val retreatHp = intent.constraints.retreatHpRatio
        if (retreatHp != null && creep.hpRatio() <= retreatHp) {
            intent.desiredPos?.let { return Action.Move(it) }
            return Action.None
        }

        intent.constraints.mustOccupyPos?.let { occupy ->
            if (getRange(creep, occupy.toHasPosition()) > 0) {
                return Action.Move(occupy)
            }
        }

        // 인접 공격
        val adjacentEnemy = world.enemyCreeps.minByOrNull { getRange(creep, it) }?.takeIf { getRange(creep, it) <= 1 }
        if (adjacentEnemy != null) return Action.Attack(adjacentEnemy.id.toString())

        // 이동
        return intent.desiredPos?.let { Action.Move(it) } ?: Action.None
    }
}

/**
 * 레인저: (1) 사거리 3 내 적이 있으면 rangedAttack
 * (2) 카이팅 모드면 적과 거리 벌리기(간단 버전: desiredPos로 이동)
 */
class RangerPolicy : RolePolicy {
    override val role: Role = Role.RANGER

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        val enemyInRange3 = world.enemyCreeps.minByOrNull { getRange(creep, it) }?.takeIf { getRange(creep, it) <= 3 }
        if (enemyInRange3 != null) return Action.RangedAttack(enemyInRange3.id.toString())
        return intent.desiredPos?.let { Action.Move(it) } ?: Action.None
    }
}

/**
 * 힐러: (1) 3칸 내 아군 중 가장 피가 낮은 대상 힐
 * (2) 없으면 desiredPos로 이동
 */
class HealerPolicy : RolePolicy {
    override val role: Role = Role.HEALER

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        val injured = world.myCreeps
            .filter { it.hits < it.hitsMax }
            .filter { getRange(creep, it) <= 3 }
            .minByOrNull { it.hits.toDouble() / it.hitsMax.toDouble() }

        if (injured != null) {
            return if (getRange(creep, injured) <= 1) Action.Heal(injured.id.toString()) else Action.RangedHeal(injured.id.toString())
        }

        return intent.desiredPos?.let { Action.Move(it) } ?: Action.None
    }
}

/**
 * 워커: draft 버전
 * - (1) 목표 타워가 있으면 에너지 넣기 (인접 시 transfer)
 * - (2) 에너지가 없으면 가까운 컨테이너에서 withdraw
 * - (3) 이동
 */
class WorkerPolicy : RolePolicy {
    override val role: Role = Role.WORKER

    override fun decide(creep: Creep, intent: UnitIntent, world: WorldModel, blackboard: Blackboard): Action {
        val energy = creep.store.getUsedCapacity(RESOURCE_ENERGY.toString()) ?: 0
        val targetTowerId = intent.focusTargetId
        if (targetTowerId != null) {
            if (energy <= 0) {
                val nearestContainer = world.containers.minByOrNull { getRange(creep, it) }
                if (nearestContainer != null) {
                    return if (getRange(creep, nearestContainer) <= 1) {
                        Action.Withdraw(nearestContainer.id.toString(), RESOURCE_ENERGY)
                    } else {
                        Action.Move(nearestContainer.toPos())
                    }
                }
            }
            val tower = world.getTowerById(targetTowerId)
            if (tower != null) {
                return if (getRange(creep, tower) <= 1) {
                    Action.Transfer(tower.id.toString(), RESOURCE_ENERGY)
                } else {
                    Action.Move(tower.toPos())
                }
            }
        }

        return intent.desiredPos?.let { Action.Move(it) } ?: Action.None
    }
}

private fun Structure.toPos(): Pos = Pos(x.toInt(), y.toInt())
