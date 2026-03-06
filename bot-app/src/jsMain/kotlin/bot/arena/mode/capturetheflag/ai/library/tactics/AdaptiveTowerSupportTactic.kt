package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Constraints
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.UnitMode
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getRange

/**
 * 전선 상황에 따라 타워 지원/후퇴를 전환하는 범용 전술.
 */
class AdaptiveTowerSupportTactic : Tactic {
    override val name: String = "AdaptiveTowerSupport"

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        if (members.isEmpty()) return

        val targetFlag = tacticContext.targetFlagId?.let(world::getFlagById)
        val targetFlagPosition = targetFlag?.toPosition() ?: tacticContext.targetPos

        val targetTowers = tacticContext.targetTowerIds
            .mapNotNull(world::getTowerById)
            .ifEmpty {
                targetFlagPosition?.let { flagPosition ->
                    world.towers
                        .filter { getRange(it, flagPosition.toHasPosition()) <= 12 }
                        .sortedBy { getRange(it, flagPosition.toHasPosition()) }
                } ?: emptyList()
            }

        val nearestTowerByMemberId = members.associate { member ->
            member.id.toString() to selectNearestNeedingEnergyTower(member, targetTowers)
        }

        val frontlineStable = isFrontlineStable(world, targetFlag)
        val fallbackPosition = tacticContext.rallyPos ?: targetFlagPosition

        members.forEach { member ->
            val targetTower = nearestTowerByMemberId[member.id.toString()]
            outputIntents[member.id.toString()] = if (frontlineStable && targetTower != null) {
                UnitIntent(
                    mode = UnitMode.Support,
                    desiredPos = targetTower.toPosition(),
                    focusTargetId = targetTower.id.toString(),
                    constraints = Constraints(retreatHpRatio = 0.3),
                )
            } else {
                UnitIntent(
                    mode = UnitMode.Retreat,
                    desiredPos = fallbackPosition,
                    constraints = Constraints(retreatHpRatio = 0.3),
                )
            }
        }
    }

    private fun isFrontlineStable(world: WorldModel, targetFlag: screeps.bindings.arena.Flag?): Boolean {
        if (targetFlag == null) return false
        val enemiesNearFlag = world.enemiesInRange(targetFlag, 10).size
        val alliesNearFlag = world.alliesInRange(targetFlag, 10).size
        return targetFlag.my == true || alliesNearFlag >= enemiesNearFlag * 2
    }

    private fun selectNearestNeedingEnergyTower(creep: Creep, towers: List<StructureTower>): StructureTower? {
        return towers
            .asSequence()
            .filter { it.needsEnergy() }
            .minByOrNull { getRange(creep, it) }
    }
}

private fun StructureTower.needsEnergy(): Boolean =
    (store.getFreeCapacity(RESOURCE_ENERGY.toString()) ?: 0) > 0

private fun screeps.bindings.arena.Flag.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun StructureTower.toPosition(): Pos = Pos(x.toInt(), y.toInt())
