package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Constraints
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.UnitMode
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.core.determineRole
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.arena.Creep
import screeps.bindings.arena.game.getRange

/**
 * 역할 기반 전열/점령 전술.
 *
 * - 근거리: 점령 우선(첫 근거리 유닛은 깃발 점유 강제)
 * - 레인저/힐러: 교전 시 카이팅 모드
 * - 테더 거리: 힐러-근거리/레인저 간 최대 5
 * - 필요 시 지원 스쿼드 단계와 연계해 지연(Hold) 가능
 */
class RoleLayeredEngagementTactic(
    private val combatLayout: CombatLayout,
    private val publishAnchorPositionStateKey: String = DEFAULT_ANCHOR_POSITION_STATE_KEY,
    private val gateSquadId: String? = null,
    private val gateStateKey: String? = null,
    private val gateReadyValue: String? = null,
    private val captureWhileEnemyDetected: Boolean = false,
) : Tactic {
    override val name: String = "RoleLayeredEngagement"

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
        val targetFlagPosition = targetFlag?.toPosition() ?: tacticContext.targetPos ?: return

        val enemyNearTarget = world.enemiesInRange(targetFlagPosition.toHasPosition(), tacticContext.engage.enemyDetectRange).isNotEmpty()
        val averageDistanceToTarget = members
            .map { getRange(it, targetFlagPosition.toHasPosition()) }
            .average()
        val almostArrived = averageDistanceToTarget <= 4.0

        val gateReady = isGateReady(blackboard)
        val shouldHoldArea = enemyNearTarget && almostArrived && !gateReady && !captureWhileEnemyDetected

        val meleeMembers = members.filter { it.determineRole() == Role.MELEE }.sortedBy { it.id.toString() }
        val rangerMembers = members.filter { it.determineRole() == Role.RANGER }.sortedBy { it.id.toString() }
        val healerMembers = members.filter { it.determineRole() == Role.HEALER }.sortedBy { it.id.toString() }
        val remainingMembers = members.filter { it !in meleeMembers && it !in rangerMembers && it !in healerMembers }

        val primaryMeleeMember = meleeMembers.firstOrNull()
        val primaryHealerMember = healerMembers.firstOrNull()

        val anchorPosition = primaryMeleeMember?.toPosition() ?: primaryHealerMember?.toPosition() ?: targetFlagPosition
        blackboard.setSquadValue(squadId, publishAnchorPositionStateKey, anchorPosition)

        val formationPositions = formationPositions(targetFlagPosition)

        meleeMembers.forEachIndexed { index, meleeMember ->
            val desiredPosition = formationPositions.meleePositions.getOrElse(index) { formationPositions.meleePositions.last() }
            outputIntents[meleeMember.id.toString()] = UnitIntent(
                mode = if (shouldHoldArea) UnitMode.Hold else UnitMode.Advance,
                desiredPos = desiredPosition,
                tetherToId = primaryHealerMember?.id?.toString(),
                constraints = Constraints(
                    maxDistanceToTether = if (primaryHealerMember != null) 5 else null,
                    mustOccupyPos = if (index == 0) targetFlagPosition else null,
                    retreatHpRatio = 0.3,
                ),
            )
        }

        rangerMembers.forEachIndexed { index, rangerMember ->
            val desiredPosition = formationPositions.rangerPositions.getOrElse(index) { formationPositions.rangerPositions.last() }
            outputIntents[rangerMember.id.toString()] = UnitIntent(
                mode = if (enemyNearTarget) UnitMode.Kite else UnitMode.Advance,
                desiredPos = desiredPosition,
                tetherToId = primaryHealerMember?.id?.toString(),
                constraints = Constraints(
                    maxDistanceToTether = if (primaryHealerMember != null) 5 else null,
                    retreatHpRatio = 0.3,
                ),
            )
        }

        healerMembers.forEachIndexed { index, healerMember ->
            val desiredPosition = formationPositions.healerPositions.getOrElse(index) { formationPositions.healerPositions.last() }
            outputIntents[healerMember.id.toString()] = UnitIntent(
                mode = if (enemyNearTarget) UnitMode.Kite else UnitMode.Support,
                desiredPos = desiredPosition,
                tetherToId = primaryMeleeMember?.id?.toString(),
                constraints = Constraints(
                    maxDistanceToTether = if (primaryMeleeMember != null) 5 else null,
                    retreatHpRatio = 0.3,
                ),
            )
        }

        val fallbackPosition = formationPositions.healerPositions.firstOrNull() ?: targetFlagPosition
        remainingMembers.forEach { member ->
            outputIntents[member.id.toString()] = UnitIntent(
                mode = if (enemyNearTarget) UnitMode.Hold else UnitMode.Advance,
                desiredPos = fallbackPosition,
                constraints = Constraints(retreatHpRatio = 0.3),
            )
        }
    }

    private fun isGateReady(blackboard: Blackboard): Boolean {
        if (gateSquadId == null || gateStateKey == null || gateReadyValue == null) return true
        val currentGateValue = blackboard.getSquadValue<String>(gateSquadId, gateStateKey)
        return currentGateValue == gateReadyValue
    }

    private fun formationPositions(targetFlagPosition: Pos): FormationPositions {
        return when (combatLayout) {
            CombatLayout.HookFrontline -> {
                val meleeBasePosition = targetFlagPosition
                val rangerBasePosition = Pos(targetFlagPosition.x + 1, targetFlagPosition.y)
                val healerBasePosition = Pos(targetFlagPosition.x, targetFlagPosition.y + 1)
                FormationPositions(
                    meleePositions = listOf(
                        meleeBasePosition,
                        Pos(meleeBasePosition.x + 1, meleeBasePosition.y),
                        Pos(meleeBasePosition.x + 2, meleeBasePosition.y),
                        Pos(meleeBasePosition.x + 3, meleeBasePosition.y),
                    ),
                    rangerPositions = listOf(
                        rangerBasePosition,
                        Pos(rangerBasePosition.x + 1, rangerBasePosition.y),
                        Pos(rangerBasePosition.x + 2, rangerBasePosition.y),
                        Pos(rangerBasePosition.x + 3, rangerBasePosition.y),
                    ),
                    healerPositions = listOf(
                        healerBasePosition,
                        Pos(healerBasePosition.x, healerBasePosition.y + 1),
                        Pos(healerBasePosition.x, healerBasePosition.y + 2),
                        Pos(healerBasePosition.x, healerBasePosition.y + 3),
                    ),
                )
            }

            CombatLayout.ThreeLineAssault -> {
                FormationPositions(
                    meleePositions = listOf(
                        targetFlagPosition,
                        Pos(targetFlagPosition.x + 1, targetFlagPosition.y),
                        Pos(targetFlagPosition.x - 1, targetFlagPosition.y),
                        Pos(targetFlagPosition.x + 2, targetFlagPosition.y),
                    ),
                    rangerPositions = listOf(
                        Pos(targetFlagPosition.x, targetFlagPosition.y + 1),
                        Pos(targetFlagPosition.x + 1, targetFlagPosition.y + 1),
                        Pos(targetFlagPosition.x - 1, targetFlagPosition.y + 1),
                        Pos(targetFlagPosition.x + 2, targetFlagPosition.y + 1),
                    ),
                    healerPositions = listOf(
                        Pos(targetFlagPosition.x, targetFlagPosition.y + 2),
                        Pos(targetFlagPosition.x + 1, targetFlagPosition.y + 2),
                        Pos(targetFlagPosition.x - 1, targetFlagPosition.y + 2),
                        Pos(targetFlagPosition.x + 2, targetFlagPosition.y + 2),
                    ),
                )
            }
        }
    }

    private data class FormationPositions(
        val meleePositions: List<Pos>,
        val rangerPositions: List<Pos>,
        val healerPositions: List<Pos>,
    )

    companion object {
        const val DEFAULT_ANCHOR_POSITION_STATE_KEY: String = "frontlineAnchorPosition"
    }
}

enum class CombatLayout {
    HookFrontline,
    ThreeLineAssault,
}

private fun screeps.bindings.arena.Flag.toPosition(): Pos = Pos(x.toInt(), y.toInt())

private fun Creep.toPosition(): Pos = Pos(x.toInt(), y.toInt())
