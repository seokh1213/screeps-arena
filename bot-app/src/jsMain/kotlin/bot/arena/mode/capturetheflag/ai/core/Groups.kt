package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.arena.Creep

/**
 * 그룹은 "멤버(크립) 집합" + "할당된 전술"의 단위.
 *
 * - Team은 Squad의 집합이지만, 실행(미시 컨트롤)은 Squad 단위로 내려간다.
 * - Director는 그룹을 split/merge/reassign 한다.
 */

sealed interface Group {
    val id: String
}

data class TeamState(
    override val id: String,
    val squadIds: MutableList<String> = mutableListOf(),
) : Group

data class SquadState(
    override val id: String,
    val memberIds: MutableList<String> = mutableListOf(),
    var assignedTactic: Tactic? = null,
    var tacticContext: TacticContext = TacticContext(),
) : Group

/**
 * Coordinator가 Executor에 전달하는 읽기 전용 뷰.
 */
data class SquadView(
    val state: SquadState,
    val members: List<Creep>,
) {
    val id: String get() = state.id
    val tactic: Tactic? get() = state.assignedTactic
    val tacticContext: TacticContext get() = state.tacticContext
}

/**
 * StrategyDirector가 만들어내는 "그룹 변경" 명령.
 *
 * - Create/Disband: Hierarchical Task Network/Utility 둘 다 쉽게 쓰기 위해 포함
 * - Split/Merge/Reassign: 더 고급 조작
 */
sealed interface GroupOperation {
    data class CreateSquad(val squadId: String, val members: List<String>) : GroupOperation
    data class DisbandSquad(val squadId: String) : GroupOperation

    data class Reassign(val creepId: String, val toSquadId: String) : GroupOperation
    data class Merge(val squadIds: List<String>, val intoSquadId: String) : GroupOperation
    data class Split(val squadId: String, val into: List<CreateSquad>) : GroupOperation
}

/**
 * Coordinator가 유지하는 그룹 저장소.
 */
class GroupStore {
    val teams: MutableMap<String, TeamState> = mutableMapOf()
    val squads: MutableMap<String, SquadState> = mutableMapOf()

    fun getOrCreateTeam(id: String): TeamState = teams.getOrPut(id) { TeamState(id) }
    fun getOrCreateSquad(id: String): SquadState = squads.getOrPut(id) { SquadState(id) }

    fun allSquadMembers(): Set<String> = squads.values.flatMap { it.memberIds }.toSet()
}
