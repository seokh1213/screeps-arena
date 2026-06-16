package bot.arena.mode.capturetheflag.behavior.teamB

import bot.arena.mode.capturetheflag.behavior.SquadState
import bot.arena.mode.capturetheflag.behavior.SquadTickResult
import bot.arena.mode.capturetheflag.behavior.actions.CreepActions
import bot.arena.mode.capturetheflag.behavior.common.*
import bot.arena.mode.capturetheflag.behavior.conditions.Conditions
import bot.arena.mode.capturetheflag.model.Context
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.game.getRange

sealed interface TeamBCombatState : SquadState {

    data class MovingToFlag(val targetFlag: Flag) : TeamBCombatState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()

            // 적 반경 10 진입 → 니은 포메이션
            val enemies = Conditions.enemiesInRange(center, 10, ctx)
            if (enemies.isNotEmpty()) {
                return NieunFormation(targetFlag).evaluate(creeps, ctx)
            }

            // 니은 포메이션으로 행군
            val assignments = NIEUN_FORMATION.assignCreeps(creeps, center, targetFlag)
            val orders = assignments.map { (creep, slot) ->
                CreepActions.moveTo(creep, slot)
            }
            return SquadTickResult(orders, this)
        }
    }

    data class NieunFormation(val targetFlag: Flag) : TeamBCombatState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val melee = creeps.firstOrNull { it.isMelee() }
            val ranger = creeps.firstOrNull { it.isRanger() }
            val healer = creeps.firstOrNull { it.isHealer() }

            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()
            val enemies = Conditions.enemiesInRange(center, 15, ctx)

            // 적 없음 → 이동 복귀
            if (enemies.isEmpty()) {
                return MovingToFlag(targetFlag).evaluate(creeps, ctx)
            }

            // HP 30% 이하 → 후퇴
            if (BattleAssessment.shouldRetreat(creeps, 0.3)) {
                return Retreating(targetFlag).evaluate(creeps, ctx)
            }

            // 포커스 파이어 대상
            val focusTarget = Targeting.priorityTarget(enemies)
            val nearestEnemy = Targeting.nearestEnemy(center, enemies)

            // 니은 포메이션 좌표 계산
            val formationCenter: HasPosition = melee ?: center
            val facingTarget: HasPosition = nearestEnemy ?: targetFlag
            val assignments = NIEUN_FORMATION.assignCreeps(creeps, formationCenter, facingTarget)

            val meleeOrder = melee?.let { unit ->
                val onFlag = getRange(unit, targetFlag) <= 1
                val anchor: HasPosition = healer ?: center

                when {
                    !onFlag && targetFlag.my != true ->
                        CreepActions.captureFlag(unit, targetFlag)
                    focusTarget != null ->
                        KitingAI.meleeLeash(unit, focusTarget, anchor, maxLeash = 5)
                    else ->
                        CreepActions.captureFlag(unit, targetFlag)
                }
            }

            val rangerOrder = ranger?.let { unit ->
                val target = Targeting.rangedFocusTarget(unit, enemies)
                val retreatAnchor: HasPosition = healer ?: center
                if (target != null) {
                    KitingAI.rangerKite(unit, target, retreatAnchor)
                } else {
                    val slot = assignments.firstOrNull { it.first == unit }?.second
                    CreepActions.moveTo(unit, slot ?: targetFlag)
                }
            }

            val healerOrder = healer?.let { unit ->
                val stayNear: HasPosition = ranger ?: melee ?: targetFlag
                HealerAI.act(unit, creeps, stayNear)
            }

            val orders = listOfNotNull(meleeOrder, rangerOrder, healerOrder)

            // 근거리가 깃발 위 + 깃발 점령됨 → HoldingFlag
            if (melee != null && getRange(melee, targetFlag) <= 1 && targetFlag.my == true) {
                return SquadTickResult(orders, HoldingFlag(targetFlag))
            }

            return SquadTickResult(orders, this)
        }
    }

    data class HoldingFlag(val targetFlag: Flag) : TeamBCombatState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val melee = creeps.firstOrNull { it.isMelee() }
            val ranger = creeps.firstOrNull { it.isRanger() }
            val healer = creeps.firstOrNull { it.isHealer() }

            // HP 30% 이하 → 후퇴
            if (BattleAssessment.shouldRetreat(creeps, 0.3)) {
                return Retreating(targetFlag).evaluate(creeps, ctx)
            }

            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()
            val enemies = Conditions.enemiesInRange(center, 15, ctx)
            val focusTarget = Targeting.focusTarget(enemies)

            val meleeOrder = melee?.let { unit ->
                if (focusTarget != null && getRange(unit, focusTarget) <= 1) {
                    CreepActions.attackTarget(unit, focusTarget)
                } else {
                    CreepActions.captureFlag(unit, targetFlag)
                }
            }

            val rangerOrder = ranger?.let { unit ->
                val target = Targeting.rangedFocusTarget(unit, enemies)
                val retreatAnchor: HasPosition = healer ?: targetFlag
                if (target != null) {
                    KitingAI.rangerKite(unit, target, retreatAnchor)
                } else {
                    CreepActions.moveTo(unit, targetFlag)
                }
            }

            val healerOrder = healer?.let { unit ->
                val stayNear: HasPosition = ranger ?: targetFlag
                HealerAI.act(unit, creeps, stayNear)
            }

            val orders = listOfNotNull(meleeOrder, rangerOrder, healerOrder)

            // 적이 없고 안정화 → Stabilized
            if (enemies.isEmpty()) {
                return SquadTickResult(orders, Stabilized(targetFlag))
            }

            // 깃발 뺏김 → NieunFormation
            if (targetFlag.my != true && melee != null) {
                return SquadTickResult(orders, NieunFormation(targetFlag))
            }

            return SquadTickResult(orders, this)
        }
    }

    data class Retreating(val targetFlag: Flag) : TeamBCombatState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()
            val enemies = Conditions.enemiesInRange(center, 15, ctx)
            val healerCenter = BattleAssessment.squadCenter(creeps.filter { it.isHealer() }) ?: center

            // HP 60% 이상 회복 → 복귀
            if (creeps.all { Conditions.hpRatio(it) >= 0.6 }) {
                return NieunFormation(targetFlag).evaluate(creeps, ctx)
            }

            val orders = creeps.map { creep ->
                when {
                    creep.isHealer() -> HealerAI.act(creep, creeps, healerCenter)
                    creep.isRanger() -> {
                        val target = Targeting.rangedFocusTarget(creep, enemies)
                        if (target != null) {
                            CreepActions.rangedAttackAndMove(creep, target, healerCenter)
                        } else {
                            CreepActions.moveTo(creep, healerCenter)
                        }
                    }
                    else -> CreepActions.moveTo(creep, healerCenter)
                }
            }

            return SquadTickResult(orders, this)
        }
    }

    data class Stabilized(val targetFlag: Flag) : TeamBCombatState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()

            // 적이 다시 나타나면 NieunFormation
            val enemies = Conditions.enemiesInRange(center, 15, ctx)
            if (enemies.isNotEmpty()) {
                return NieunFormation(targetFlag).evaluate(creeps, ctx)
            }

            // 깃발 뺏겼으면 다시 확보
            if (targetFlag.my != true) {
                return MovingToFlag(targetFlag).evaluate(creeps, ctx)
            }

            // 깃발 수 비교 → 공세 or 방어
            val myFlagCount = ctx.myFlags.size
            val enemyFlagCount = ctx.enemyFlags.size

            val nextTarget = if (myFlagCount <= enemyFlagCount) {
                ctx.enemyFlags.minByOrNull { getRange(center, it) }
            } else {
                ctx.myFlags.minByOrNull { getRange(center, it) }
            }

            if (nextTarget != null && nextTarget != targetFlag) {
                return SquadTickResult(
                    creeps.map { CreepActions.moveTo(it, nextTarget) },
                    MovingToFlag(nextTarget)
                )
            }

            val orders = creeps.map { CreepActions.moveTo(it, targetFlag) }
            return SquadTickResult(orders, this)
        }
    }
}
