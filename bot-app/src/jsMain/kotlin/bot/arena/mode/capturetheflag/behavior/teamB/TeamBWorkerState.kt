package bot.arena.mode.capturetheflag.behavior.teamB

import bot.arena.mode.capturetheflag.behavior.SquadState
import bot.arena.mode.capturetheflag.behavior.SquadTickResult
import bot.arena.mode.capturetheflag.behavior.actions.CreepActions
import bot.arena.mode.capturetheflag.behavior.conditions.Conditions
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.Order
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

sealed interface TeamBWorkerState : SquadState {

    data class ChargingHomeTower(
        val homeTower: StructureTower,
        val targetFlag: Flag,
    ) : TeamBWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            // 에너지 없으면 보충
            if (!Conditions.hasEnergy(worker)) {
                val container = Conditions.nearestContainerWithEnergy(worker, ctx)
                if (container != null) {
                    return Resupplying(container, this).evaluate(creeps, ctx)
                }
            }

            // 타워 충전 완료 → B-Combat 합류
            if (!Conditions.towerNeedsEnergy(homeTower)) {
                return SquadTickResult(
                    listOf(CreepActions.moveTo(worker, targetFlag)),
                    MovingToCombat(targetFlag)
                )
            }

            return SquadTickResult(
                listOf(CreepActions.chargeTower(worker, homeTower)),
                this
            )
        }
    }

    data class MovingToCombat(val targetFlag: Flag) : TeamBWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            // 깃발 근처 도착 → 타워 충전으로 전환
            if (getRange(worker, targetFlag) <= 5) {
                val flagTower = ctx.towers
                    .filter { getRange(it, targetFlag) <= 10 }
                    .firstOrNull { Conditions.towerNeedsEnergy(it) }

                if (flagTower != null) {
                    return ChargingFlagTower(flagTower, targetFlag).evaluate(creeps, ctx)
                }
                // 타워 충전 불필요 → 깃발 근처 대기
                return SquadTickResult(
                    listOf(CreepActions.moveTo(worker, targetFlag)),
                    this
                )
            }

            // 적 회피
            val enemies = Conditions.enemiesInRange(worker, 5, ctx)
            if (enemies.isNotEmpty()) {
                val nearestEnemy = enemies.minBy { getRange(worker, it) }
                return SquadTickResult(
                    listOf(CreepActions.retreat(worker, nearestEnemy)),
                    this
                )
            }

            return SquadTickResult(
                listOf(CreepActions.moveTo(worker, targetFlag)),
                this
            )
        }
    }

    data class ChargingFlagTower(
        val tower: StructureTower,
        val targetFlag: Flag,
    ) : TeamBWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            // 에너지 없으면 보충
            if (!Conditions.hasEnergy(worker)) {
                val container = Conditions.nearestContainerWithEnergy(worker, ctx)
                if (container != null) {
                    return Resupplying(container, this).evaluate(creeps, ctx)
                }
            }

            // 타워 충전 완료 → 안정화 판단
            if (!Conditions.towerNeedsEnergy(tower)) {
                // 다른 충전할 타워 탐색
                val nextTower = ctx.towers
                    .filter { getRange(it, targetFlag) <= 10 }
                    .firstOrNull { Conditions.towerNeedsEnergy(it) && it != tower }

                return if (nextTower != null) {
                    ChargingFlagTower(nextTower, targetFlag).evaluate(creeps, ctx)
                } else {
                    // 전부 충전 완료 → 대기 (Stabilized 후 다음 목표로)
                    SquadTickResult(
                        listOf(CreepActions.moveTo(worker, targetFlag)),
                        WaitingForNextMission(targetFlag)
                    )
                }
            }

            // 적 회피
            val enemies = Conditions.enemiesInRange(worker, 5, ctx)
            if (enemies.isNotEmpty()) {
                val nearestEnemy = enemies.minBy { getRange(worker, it) }
                return SquadTickResult(
                    listOf(CreepActions.retreat(worker, nearestEnemy)),
                    this
                )
            }

            return SquadTickResult(
                listOf(CreepActions.chargeTower(worker, tower)),
                this
            )
        }
    }

    data class Resupplying(
        val container: StructureContainer,
        val returnTo: TeamBWorkerState,
    ) : TeamBWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            if (Conditions.hasEnergy(worker)) {
                return returnTo.evaluate(creeps, ctx)
            }

            return SquadTickResult(
                listOf(CreepActions.withdrawEnergy(worker, container)),
                this
            )
        }
    }

    data class WaitingForNextMission(val currentFlag: Flag) : TeamBWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            // 충전할 타워가 다시 생기면 충전
            val needsCharge = ctx.towers
                .filter { it.my == true }
                .firstOrNull { Conditions.towerNeedsEnergy(it) }

            if (needsCharge != null) {
                val nearHome = getRange(needsCharge, currentFlag) > 20
                return if (nearHome) {
                    // 홈 타워 → ChargingHomeTower
                    ChargingHomeTower(needsCharge, currentFlag).evaluate(creeps, ctx)
                } else {
                    ChargingFlagTower(needsCharge, currentFlag).evaluate(creeps, ctx)
                }
            }

            // 대기: 깃발 근처에 머무름
            return SquadTickResult(
                listOf(CreepActions.moveTo(worker, currentFlag)),
                this
            )
        }
    }
}
