package bot.arena.mode.capturetheflag.strategy

import bot.arena.mode.capturetheflag.behavior.teamA.TeamAAttackState
import bot.arena.mode.capturetheflag.behavior.teamA.TeamAWorkerState
import bot.arena.mode.capturetheflag.behavior.teamB.TeamBCombatState
import bot.arena.mode.capturetheflag.behavior.teamB.TeamBWorkerState
import bot.arena.mode.capturetheflag.model.BattlePlan
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.CreepRole.HEALER
import bot.arena.mode.capturetheflag.model.CreepRole.MELEE
import bot.arena.mode.capturetheflag.model.CreepRole.RANGER
import bot.arena.mode.capturetheflag.model.CreepRole.WORKER
import bot.arena.mode.capturetheflag.model.StrategyDirector
import bot.arena.mode.capturetheflag.model.battlePlan
import screeps.bindings.arena.Flag
import screeps.bindings.arena.game.getTicks

enum class Strategy {
    AGGRESSIVE,
    DEFENSIVE,
    ALL_IN,
}

class CTFStrategyDirector : StrategyDirector {

    private var flagA: Flag? = null
    private var flagB: Flag? = null

    override fun plan(ctx: Context): BattlePlan {
        assignFlags(ctx)
        val targetFlagA = flagA ?: return BattlePlan(emptyList())
        val targetFlagB = flagB ?: return BattlePlan(emptyList())

        return when (evaluateStrategy(ctx)) {
            Strategy.AGGRESSIVE -> aggressivePlan(ctx, targetFlagA, targetFlagB)
            Strategy.DEFENSIVE -> defensivePlan(ctx, targetFlagA, targetFlagB)
            Strategy.ALL_IN -> allInPlan(ctx, targetFlagA)
        }
    }

    private fun evaluateStrategy(ctx: Context): Strategy {
        val myAlive = ctx.myCreeps.size
        val enemyAlive = ctx.enemyCreeps.size
        val myFlagCount = ctx.myFlags.size
        val tick = getTicks()

        return when {
            // 병력이 크게 부족하면 수비
            myAlive <= 4 -> Strategy.DEFENSIVE
            // 깃발 우위면 수비적으로 유지
            myFlagCount >= 3 -> Strategy.DEFENSIVE
            // 후반 열세면 올인
            tick > 1500 && myFlagCount < enemyAlive -> Strategy.ALL_IN
            // 기본: 공격
            else -> Strategy.AGGRESSIVE
        }
    }

    // --- 공격적 전략 (기본) ---
    private fun aggressivePlan(ctx: Context, flagA: Flag, flagB: Flag): BattlePlan =
        battlePlan(ctx) {
            team("Team-A") {
                squad("A-Attack") {
                    role(MELEE, min = 1, max = 3)
                    role(RANGER, min = 1, max = 3)
                    role(HEALER, min = 1, max = 3)
                    behavior { _, _ ->
                        TeamAAttackState.Advancing(flagA)
                    }
                }
                squad("A-Worker") {
                    role(WORKER, min = 1)
                    behavior { _, _ ->
                        TeamAWorkerState.Following(flagA)
                    }
                }
            }
            team("Team-B") {
                squad("B-Combat") {
                    role(MELEE, min = 1)
                    role(RANGER, min = 1)
                    role(HEALER, min = 1)
                    behavior { _, _ ->
                        TeamBCombatState.MovingToFlag(flagB)
                    }
                }
                squad("B-Support") {
                    role(WORKER, min = 1)
                    behavior { initCtx, _ ->
                        val homeTower = initCtx.myTowers.firstOrNull()
                        if (homeTower != null) {
                            TeamBWorkerState.ChargingHomeTower(homeTower, flagB)
                        } else {
                            TeamBWorkerState.MovingToCombat(flagB)
                        }
                    }
                }
            }
        }

    // --- 수비적 전략: 하나의 깃발에 집중, 나머지 방어 ---
    private fun defensivePlan(ctx: Context, flagA: Flag, flagB: Flag): BattlePlan =
        battlePlan(ctx) {
            // 병력이 적으므로 하나의 팀에 집중
            team("Def-Main") {
                squad("Def-Combat") {
                    role(MELEE, min = 1, max = 4)
                    role(RANGER, min = 1, max = 4)
                    role(HEALER, min = 1, max = 4)
                    behavior { _, _ ->
                        // 가장 가까운 아군 깃발 방어
                        val defendFlag = ctx.myFlags.firstOrNull() ?: flagA
                        TeamAAttackState.Engaging(defendFlag)
                    }
                }
                squad("Def-Worker") {
                    role(WORKER, min = 1, max = 2)
                    behavior { initCtx, _ ->
                        val tower = initCtx.myTowers.firstOrNull()
                        val defendFlag = ctx.myFlags.firstOrNull() ?: flagA
                        if (tower != null) {
                            TeamAWorkerState.ChargingTower(tower, defendFlag)
                        } else {
                            TeamAWorkerState.Following(defendFlag)
                        }
                    }
                }
            }
        }

    // --- 올인 전략: 전원 한 곳에 집중 ---
    private fun allInPlan(ctx: Context, flagA: Flag): BattlePlan =
        battlePlan(ctx) {
            team("AllIn") {
                squad("AllIn-Combat") {
                    role(MELEE, min = 1, max = 4)
                    role(RANGER, min = 1, max = 4)
                    role(HEALER, min = 1, max = 4)
                    behavior { _, _ ->
                        // 적 깃발 중 가장 가까운 곳 공격
                        val target = ctx.enemyFlags.firstOrNull() ?: flagA
                        TeamAAttackState.Advancing(target)
                    }
                }
                squad("AllIn-Worker") {
                    role(WORKER, min = 1, max = 2)
                    behavior { _, _ ->
                        val target = ctx.enemyFlags.firstOrNull() ?: flagA
                        TeamAWorkerState.Following(target)
                    }
                }
            }
        }

    private fun assignFlags(ctx: Context) {
        if (flagA != null && flagB != null) return

        val neutralFlags = ctx.neutralFlags
        if (neutralFlags.size < 2) return

        // 시계 방향 할당: y, x 좌표 기준 정렬 후 첫번째 → B, 두번째 → A
        val sorted = neutralFlags.sortedWith(compareBy({ it.y }, { it.x }))
        flagB = sorted[0]
        flagA = sorted[1]
    }
}
