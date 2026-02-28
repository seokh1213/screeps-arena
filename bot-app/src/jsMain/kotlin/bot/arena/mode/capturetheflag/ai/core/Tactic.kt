package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.arena.Creep

/**
 * 전술(Tactic) = 스쿼드 단위 행동 생성기.
 *
 * - Utility/Hierarchical Task Network은 "어떤 전술을 어느 스쿼드가 할지"만 결정한다.
 * - 세부 전투 규칙(카이팅/힐 우선/리쉬 등)은 RolePolicy로 내려간다.
 */
interface Tactic {
    val name: String

    /**
     * 한 틱 동안의 유닛별 의도(intent)를 생성한다.
     * - outputIntents: creepId -> intent
     */
    fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    )

    /**
     * 전술의 완료 조건(선택). 완료되면 Director가 다른 전술로 교체할 수 있다.
     */
    fun isComplete(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
    ): Boolean = false
}

data class TacticContext(
    val targetPos: Pos? = null,
    val targetFlagId: String? = null,
    val targetTowerIds: List<String> = emptyList(),
    val rallyPos: Pos? = null,
    val formation: FormationSpec? = null,
    val engage: EngageRules = EngageRules(),
)

data class EngageRules(
    val enemyDetectRange: Int = 10,
    val stabilizeTicks: Int = 25,
)

enum class UnitMode {
    Advance,
    Hold,
    Kite,
    Retreat,
    Support,
}

data class UnitIntent(
    val mode: UnitMode,
    val desiredPos: Pos? = null,
    val focusTargetId: String? = null,
    val tetherToId: String? = null,
    val constraints: Constraints = Constraints(),
)

data class Constraints(
    val maxDistanceToTether: Int? = null,
    val mustOccupyPos: Pos? = null,
    val retreatHpRatio: Double? = null,
)
