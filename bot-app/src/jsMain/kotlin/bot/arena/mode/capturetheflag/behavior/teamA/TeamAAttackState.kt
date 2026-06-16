package bot.arena.mode.capturetheflag.behavior.teamA

import bot.arena.mode.capturetheflag.behavior.SquadState
import bot.arena.mode.capturetheflag.behavior.SquadTickResult
import bot.arena.mode.capturetheflag.behavior.actions.CreepActions
import bot.arena.mode.capturetheflag.behavior.common.*
import bot.arena.mode.capturetheflag.behavior.conditions.Conditions
import bot.arena.mode.capturetheflag.model.Context
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.game.getRange

sealed interface TeamAAttackState : SquadState {

    data class Advancing(val targetFlag: Flag) : TeamAAttackState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()
            val enemies = Conditions.enemiesInRange(center, 15, ctx)

            if (enemies.isNotEmpty()) {
                return Engaging(targetFlag).evaluate(creeps, ctx)
            }

            // 포메이션 행군: LINE_FORMATION으로 깃발을 향해 이동
            val assignments = LINE_FORMATION.assignCreeps(creeps, center, targetFlag)
            val orders = assignments.map { (creep, slot) ->
                CreepActions.moveTo(creep, slot)
            }

            return SquadTickResult(orders, this)
        }
    }

    data class Engaging(val targetFlag: Flag) : TeamAAttackState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val melees = creeps.filter { it.isMelee() }
            val rangers = creeps.filter { it.isRanger() }
            val healers = creeps.filter { it.isHealer() }

            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()
            val enemies = Conditions.enemiesInRange(center, 20, ctx)

            // 적이 없으면 Advancing 복귀
            if (enemies.isEmpty()) {
                return Advancing(targetFlag).evaluate(creeps, ctx)
            }

            // 다수 HP 40% 이하 → Kiting
            if (creeps.count { Conditions.hpBelow(it, 0.4) } >= 2) {
                return Kiting(targetFlag).evaluate(creeps, ctx)
            }

            // 포커스 파이어 대상
            val focusTarget = Targeting.priorityTarget(enemies)
            val healerCenter = BattleAssessment.squadCenter(healers) ?: center

            val meleeOrders = melees.map { melee ->
                val onFlag = getRange(melee, targetFlag) <= 1
                when {
                    !onFlag && targetFlag.my != true ->
                        CreepActions.captureFlag(melee, targetFlag)
                    focusTarget != null ->
                        KitingAI.meleeLeash(melee, focusTarget, healerCenter, maxLeash = 5)
                    else ->
                        CreepActions.captureFlag(melee, targetFlag)
                }
            }

            val rangerOrders = rangers.map { ranger ->
                val target = Targeting.rangedFocusTarget(ranger, enemies)
                if (target != null) {
                    KitingAI.rangerKite(ranger, target, healerCenter)
                } else {
                    CreepActions.moveTo(ranger, targetFlag)
                }
            }

            val healerOrders = healers.map { healer ->
                HealerAI.act(healer, creeps, healerCenter)
            }

            val orders = meleeOrders + rangerOrders + healerOrders

            return SquadTickResult(orders, this)
        }
    }

    data class Kiting(val targetFlag: Flag) : TeamAAttackState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val healers = creeps.filter { it.isHealer() }

            val center = BattleAssessment.squadCenter(creeps) ?: creeps.first()
            val enemies = Conditions.enemiesInRange(center, 20, ctx)
            val healerCenter = BattleAssessment.squadCenter(healers) ?: center

            // HP 60% 이상 회복 or 적 없음 → 재진입
            val allRecovered = creeps.all { Conditions.hpRatio(it) >= 0.6 }
            if (allRecovered || enemies.isEmpty()) {
                return Engaging(targetFlag).evaluate(creeps, ctx)
            }

            // 수비 대형으로 뭉치며 후퇴
            val assignments = DEFENSIVE_FORMATION.assignCreeps(creeps, healerCenter, center)

            val orders = assignments.map { (creep, slot) ->
                when {
                    creep.isHealer() -> HealerAI.act(creep, creeps, slot)
                    creep.isRanger() -> {
                        val target = Targeting.rangedFocusTarget(creep, enemies)
                        if (target != null) {
                            CreepActions.rangedAttackAndMove(creep, target, slot)
                        } else {
                            CreepActions.moveTo(creep, slot)
                        }
                    }
                    else -> CreepActions.moveTo(creep, slot)
                }
            }

            return SquadTickResult(orders, this)
        }
    }
}
