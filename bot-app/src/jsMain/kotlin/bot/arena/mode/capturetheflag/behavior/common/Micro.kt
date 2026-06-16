package bot.arena.mode.capturetheflag.behavior.common

import bot.arena.mode.capturetheflag.behavior.actions.CreepActions
import bot.arena.mode.capturetheflag.behavior.conditions.Conditions
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.arena.Creep
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.game.getRange

// --- 타겟팅 ---

object Targeting {
    /**
     * 포커스 파이어 대상 선정: HP가 가장 낮은 적 우선 (원킬 가능성 극대화)
     */
    fun focusTarget(enemies: List<Creep>): Creep? =
        enemies.minByOrNull { it.hits }

    /**
     * 가장 가까운 적
     */
    fun nearestEnemy(from: HasPosition, enemies: List<Creep>): Creep? =
        enemies.minByOrNull { getRange(from, it) }

    /**
     * 힐러 우선 제거 대상 (적 힐러를 먼저 죽이면 유리)
     */
    fun priorityTarget(enemies: List<Creep>): Creep? {
        val healers = enemies.filter { it.isHealer() }
        if (healers.isNotEmpty()) return healers.minByOrNull { it.hits }
        return focusTarget(enemies)
    }

    /**
     * 레인저 사거리 내 가장 약한 적
     */
    fun rangedFocusTarget(from: HasPosition, enemies: List<Creep>, maxRange: Int = 3): Creep? {
        val inRange = enemies.filter { getRange(from, it) <= maxRange }
        return inRange.minByOrNull { it.hits } ?: nearestEnemy(from, enemies)
    }
}

// --- 힐러 AI ---

object HealerAI {
    /**
     * 힐 대상 선정 우선순위:
     * 1. HP 30% 이하 긴급
     * 2. HP가 가장 낮은 아군
     * 3. 근거리 크립 (항상 데미지 받음)
     */
    fun selectHealTarget(healer: Creep, allies: List<Creep>): Creep? {
        val others = allies.filter { it != healer }
        if (others.isEmpty()) return null

        // 긴급 치유
        val critical = others.filter { Conditions.hpBelow(it, 0.3) }
        if (critical.isNotEmpty()) return critical.minByOrNull { getRange(healer, it) }

        // 가장 HP 비율 낮은 아군
        val damaged = others.filter { Conditions.hpRatio(it) < 1.0 }
        if (damaged.isNotEmpty()) return damaged.minByOrNull { Conditions.hpRatio(it) }

        // 전원 풀피 → 근거리 크립 선제 치유 준비
        return others.firstOrNull { it.isMelee() }
    }

    /**
     * 힐러 행동: 치유 + 위치 이동
     */
    fun act(healer: Creep, allies: List<Creep>, stayNear: HasPosition): Order<Creep> {
        val target = selectHealTarget(healer, allies)
        return if (target != null && Conditions.hpRatio(target) < 1.0) {
            CreepActions.healAndMove(healer, target, stayNear)
        } else {
            CreepActions.moveTo(healer, stayNear)
        }
    }
}

// --- 카이팅 AI ---

object KitingAI {
    /**
     * 레인저 카이팅: 사거리 3 유지하면서 공격
     * - 거리 <= 2: 공격 + 후퇴
     * - 거리 == 3: 공격만
     * - 거리 > 3: 접근
     */
    fun rangerKite(
        ranger: Creep,
        target: Creep,
        retreatAnchor: HasPosition,
    ): Order<Creep> {
        val dist = getRange(ranger, target)
        return when {
            dist <= 2 -> CreepActions.rangedAttackAndMove(ranger, target, retreatAnchor)
            dist <= 3 -> CreepActions.rangedAttackTarget(ranger, target)
            else -> CreepActions.moveTo(ranger, target)
        }
    }

    /**
     * 근거리 리쉬 공격: 힐러와 maxLeash 거리 이내에서만 전진
     * - 리쉬 초과: 제자리 공격만
     * - 리쉬 이내: 공격 + 전진
     */
    fun meleeLeash(
        melee: Creep,
        target: Creep,
        anchor: HasPosition,
        maxLeash: Int = 5,
    ): Order<Creep> {
        val distToAnchor = getRange(melee, anchor)
        return if (distToAnchor >= maxLeash) {
            // 리쉬 한계 → 접근하지 말고 사거리 내 공격만
            if (getRange(melee, target) <= 1) {
                CreepActions.attackTarget(melee, target)
            } else {
                CreepActions.moveTo(melee, anchor)
            }
        } else {
            CreepActions.attackTarget(melee, target)
        }
    }
}

// --- 전선 판단 ---

object BattleAssessment {
    /**
     * 전선을 밀 수 있는 상황인지 판단
     */
    fun shouldPush(myCreeps: List<Creep>, nearbyEnemies: List<Creep>): Boolean {
        if (nearbyEnemies.isEmpty()) return true
        val myAvgHp = myCreeps.map { Conditions.hpRatio(it) }.average()
        val enemyAvgHp = nearbyEnemies.map { Conditions.hpRatio(it) }.average()
        return myAvgHp > enemyAvgHp + 0.1 && myCreeps.size >= nearbyEnemies.size
    }

    /**
     * 후퇴해야 하는 상황인지 판단
     */
    fun shouldRetreat(creeps: List<Creep>, hpThreshold: Double = 0.3): Boolean =
        creeps.any { Conditions.hpBelow(it, hpThreshold) }

    /**
     * 스쿼드 중심점 계산
     */
    fun squadCenter(creeps: List<Creep>): HasPosition? {
        if (creeps.isEmpty()) return null
        val cx = creeps.map { it.x }.average()
        val cy = creeps.map { it.y }.average()
        return object : HasPosition {
            override var x = cx
            override var y = cy
        }
    }
}
