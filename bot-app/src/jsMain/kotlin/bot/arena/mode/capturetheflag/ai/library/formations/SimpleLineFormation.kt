package bot.arena.mode.capturetheflag.ai.library.formations

import bot.arena.mode.capturetheflag.ai.core.FormationSpec
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.arena.Creep

/**
 * 아주 단순한 3열(line) 진형.
 *
 * - anchor를 기준으로 y+row, x+col 형태로 슬롯을 배치한다.
 * - 실제로는 지형/장애물/적 방향에 따라 회전/플립이 필요하지만, 초안에서는 고정.
 */
class SimpleLineFormation(
    private val width: Int = 3,
) : FormationSpec {
    override val name: String = "SimpleLine"

    override fun computeSlots(anchor: Pos, members: List<Creep>, world: WorldModel): Map<String, Pos> {
        val sorted = members.sortedBy { it.id.toString() }
        return sorted.mapIndexed { i, creep ->
            val row = i / width
            val col = i % width
            val pos = Pos(anchor.x + col, anchor.y + row)
            creep.id.toString() to pos
        }.toMap()
    }
}
