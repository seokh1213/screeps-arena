package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.arena.HasPosition
import screeps.interop.jsObject

/**
 * Screeps Arena의 좌표는 [HasPosition] (x/y: Double)로 표현된다.
 * AI 레이어에서는 정수 기반 [Pos]를 쓰고, 필요할 때만 [HasPosition]로 변환한다.
 */
data class Pos(val x: Int, val y: Int) {
    fun toHasPosition(): HasPosition = jsObject<HasPosition> {
        this.x = this@Pos.x.toDouble()
        this.y = this@Pos.y.toDouble()
    }
}

fun HasPosition.toPos(): Pos = Pos(x.toInt(), y.toInt())
