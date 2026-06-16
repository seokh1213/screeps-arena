package bot.arena.mode.capturetheflag.behavior.teamA

import bot.arena.mode.capturetheflag.behavior.SquadState
import bot.arena.mode.capturetheflag.behavior.SquadTickResult
import bot.arena.mode.capturetheflag.behavior.actions.CreepActions
import bot.arena.mode.capturetheflag.behavior.conditions.Conditions
import bot.arena.mode.capturetheflag.model.Context
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

sealed interface TeamAWorkerState : SquadState {

    data class Following(val targetFlag: Flag) : TeamAWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            // 깃발 근처 타워를 찾아서 충전 기회 탐색
            val nearbyTower = ctx.towers
                .asSequence()
                .filter { it.my != false }
                .filter { getRange(it, targetFlag) <= 10 }
                .firstOrNull(Conditions::towerNeedsEnergy)

            if (nearbyTower != null && getRange(worker, nearbyTower) <= 5) {
                // 타워가 가깝고 에너지 필요 → 충전 모드
                return if (!Conditions.hasEnergy(worker)) {
                    val container = Conditions.nearestContainerWithEnergy(worker, ctx)
                    if (container != null) {
                        Resupplying(container, ChargingTower(nearbyTower, targetFlag)).evaluate(creeps, ctx)
                    } else {
                        SquadTickResult(listOf(CreepActions.moveTo(worker, targetFlag)), this)
                    }
                } else {
                    ChargingTower(nearbyTower, targetFlag).evaluate(creeps, ctx)
                }
            }

            // 적이 가까우면 후퇴
            val order = Conditions.enemiesInRange(worker, 8, ctx)
                .minByOrNull { getRange(worker, it) }
                ?.let { enemy -> CreepActions.retreat(worker, enemy) }
                ?: CreepActions.moveTo(worker, targetFlag)

            return SquadTickResult(listOf(order), this)
        }
    }

    data class ChargingTower(
        val tower: StructureTower,
        val targetFlag: Flag,
    ) : TeamAWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            // 에너지 없으면 보충
            if (!Conditions.hasEnergy(worker)) {
                val container = Conditions.nearestContainerWithEnergy(worker, ctx)
                if (container != null) {
                    return Resupplying(container, this).evaluate(creeps, ctx)
                }
                return SquadTickResult(
                    listOf(CreepActions.moveTo(worker, targetFlag)),
                    Following(targetFlag)
                )
            }

            // 타워 충전 불필요 → Following으로 복귀
            if (!Conditions.towerNeedsEnergy(tower)) {
                return SquadTickResult(
                    listOf(CreepActions.moveTo(worker, targetFlag)),
                    Following(targetFlag)
                )
            }

            // 적 접근 시 후퇴
            val nearestEnemy = Conditions.enemiesInRange(worker, 5, ctx)
                .minByOrNull { getRange(worker, it) }

            if (nearestEnemy != null) {
                return SquadTickResult(
                    listOf(CreepActions.retreat(worker, nearestEnemy)),
                    Following(targetFlag)
                )
            }

            return SquadTickResult(listOf(CreepActions.chargeTower(worker, tower)), this)
        }
    }

    data class Resupplying(
        val container: StructureContainer,
        val returnTo: TeamAWorkerState,
    ) : TeamAWorkerState {
        override fun evaluate(creeps: List<Creep>, ctx: Context): SquadTickResult {
            val worker = creeps.firstOrNull()
                ?: return SquadTickResult(emptyList(), this)

            if (Conditions.isEnergyFull(worker) || Conditions.hasEnergy(worker)) {
                return returnTo.evaluate(creeps, ctx)
            }

            return SquadTickResult(listOf(CreepActions.withdrawEnergy(worker, container)), this)
        }
    }
}
