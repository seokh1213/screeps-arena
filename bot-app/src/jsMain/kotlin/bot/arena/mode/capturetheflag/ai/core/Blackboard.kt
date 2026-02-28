package bot.arena.mode.capturetheflag.ai.core

/**
 * Director / Coordinator / Executor가 공유하는 "작전판".
 * - 한 틱에서 계산한 결과를 다음 틱에 유지해야 할 때(타겟 플래그 선택, 집결지 등)
 * - 또는 Hierarchical Task Network/Utility 양쪽에서 공통으로 쓰는 상태 저장소
 */
class Blackboard {
    /**
     * 예: TeamB가 선택한 중립 플래그
     */
    var primaryTargetFlagId: String? = null

    /**
     * 예: TeamA가 선택한 다른 중립 플래그
     */
    var secondaryTargetFlagId: String? = null

    /**
     * 스쿼드별 상태 저장
     */
    val squadState: MutableMap<String, MutableMap<String, Any>> = mutableMapOf()

    fun <T : Any> getSquadValue(squadId: String, key: String): T? =
        squadState[squadId]?.get(key) as? T

    fun setSquadValue(squadId: String, key: String, value: Any) {
        val map = squadState.getOrPut(squadId) { mutableMapOf() }
        map[key] = value
    }
}
