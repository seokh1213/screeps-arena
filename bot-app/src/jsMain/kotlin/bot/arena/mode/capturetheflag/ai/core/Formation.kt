package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.arena.Creep

/**
 * 진형(formation)은 "슬롯 좌표"를 계산해서 유닛에게 desiredPos로 내려주는 역할만 한다.
 * - 전투 규칙은 RolePolicy
 * - 목표 선택은 Director
 */
interface FormationSpec {
    val name: String

    /**
     * @return creepId -> slotPos
     */
    fun computeSlots(
        anchor: Pos,
        members: List<Creep>,
        world: WorldModel,
    ): Map<String, Pos>
}

/**
 * 기본: 모든 멤버가 anchor로 집결.
 */
object StackFormation : FormationSpec {
    override val name: String = "Stack"

    override fun computeSlots(anchor: Pos, members: List<Creep>, world: WorldModel): Map<String, Pos> =
        members.associate { it.id.toString() to anchor }
}
