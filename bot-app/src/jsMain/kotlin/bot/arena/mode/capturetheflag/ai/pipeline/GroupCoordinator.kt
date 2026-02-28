package bot.arena.mode.capturetheflag.ai.pipeline

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.GroupOperation
import bot.arena.mode.capturetheflag.ai.core.GroupStore
import bot.arena.mode.capturetheflag.ai.core.SquadState
import bot.arena.mode.capturetheflag.ai.core.SquadView
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.TacticAssignment
import bot.arena.mode.capturetheflag.ai.core.WorldModel

/**
 * GroupCoordinator = Director가 만든 계획을 실제 그룹 구조(스쿼드/팀)에 반영한다.
 *
 * - split/merge/reassign 적용
 * - 스쿼드에 전술(Tactic) 할당
 * - 그리고 "이번 틱에 실행할 스쿼드 목록"을 만들어 Executor로 넘긴다.
 */
class GroupCoordinator(
    private val groups: GroupStore,
) {
    fun apply(plan: StrategyPlan, world: WorldModel, blackboard: Blackboard): List<SquadView> {
        applyOperations(plan.operations)
        applyAssignments(plan.assignments)

        // 존재하는 creep만 유지
        val existingIds = world.myCreeps.map { it.id.toString() }.toSet()
        groups.squads.values.forEach { squad ->
            squad.memberIds.retainAll(existingIds)
        }

        // SquadView 생성
        return groups.squads.values
            .filter { it.memberIds.isNotEmpty() }
            .map { squad ->
                val members = squad.memberIds.mapNotNull { world.getCreepById(it) }
                SquadView(state = squad, members = members)
            }
    }

    private fun applyAssignments(assignments: List<TacticAssignment>) {
        assignments.forEach { a ->
            val squad = groups.getOrCreateSquad(a.squadId)
            squad.assignedTactic = a.tactic
            squad.tacticContext = a.context
        }
    }

    private fun applyOperations(ops: List<GroupOperation>) {
        ops.forEach { op ->
            when (op) {
                is GroupOperation.CreateSquad -> {
                    val squad = groups.getOrCreateSquad(op.squadId)
                    squad.memberIds.clear()
                    squad.memberIds.addAll(op.members)
                }

                is GroupOperation.DisbandSquad -> {
                    groups.squads.remove(op.squadId)
                }

                is GroupOperation.Reassign -> {
                    // 다른 스쿼드에서 제거
                    groups.squads.values.forEach { it.memberIds.remove(op.creepId) }
                    groups.getOrCreateSquad(op.toSquadId).memberIds.add(op.creepId)
                }

                is GroupOperation.Merge -> {
                    val into = groups.getOrCreateSquad(op.intoSquadId)
                    val merged = op.squadIds.flatMap { sid -> groups.squads[sid]?.memberIds ?: emptyList() }
                    into.memberIds.clear(); into.memberIds.addAll(merged.distinct())
                    op.squadIds.filter { it != op.intoSquadId }.forEach { groups.squads.remove(it) }
                }

                is GroupOperation.Split -> {
                    val origin: SquadState = groups.squads[op.squadId] ?: return@forEach
                    // 원본 삭제 후 신규 스쿼드 생성
                    groups.squads.remove(op.squadId)
                    op.into.forEach { create ->
                        val s = groups.getOrCreateSquad(create.squadId)
                        s.memberIds.clear(); s.memberIds.addAll(create.members)
                    }
                    // 안전: origin 멤버가 누락됐으면 버림(초안에서는 엄격하게 추적하지 않음)
                }
            }
        }
    }
}
