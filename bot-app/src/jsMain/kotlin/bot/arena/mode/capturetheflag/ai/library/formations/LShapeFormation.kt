package bot.arena.mode.capturetheflag.ai.library.formations

import bot.arena.mode.capturetheflag.ai.core.FormationSpec
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.arena.Creep

/**
 * ㄴ(니은) 모양 진형의 초안.
 * - anchor를 코너로 두고 오른쪽/아래로 뻗는다.
 * - 실제 전투에서는 적 방향에 맞춰 회전이 필요하지만, 인터페이스 초안 목적이라 고정.
 */
class LShapeFormation(
    private val armLen: Int = 2,
) : FormationSpec {
    override val name: String = "LShape"

    override fun computeSlots(anchor: Pos, members: List<Creep>, world: WorldModel): Map<String, Pos> {
        val sorted = members.sortedBy { it.id.toString() }

        val slots = mutableListOf<Pos>()
        slots += anchor
        // x arm
        for (i in 1..armLen) slots += Pos(anchor.x + i, anchor.y)
        // y arm
        for (i in 1..armLen) slots += Pos(anchor.x, anchor.y + i)

        // 멤버 수가 슬롯보다 많으면 나머지는 anchor 근처로 뭉친다.
        return sorted.mapIndexed { idx, creep ->
            val pos = slots.getOrNull(idx) ?: anchor
            creep.id.toString() to pos
        }.toMap()
    }
}
