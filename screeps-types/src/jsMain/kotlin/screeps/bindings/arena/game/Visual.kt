@file:JsModule("game/visual")
@file:JsNonModule

package screeps.bindings.arena.game

import screeps.bindings.Options
import screeps.bindings.arena.HasPosition

typealias Color = String
typealias LineStyle = String
typealias TextAlign = String

external class Visual(layer: Int = definedExternally, persistent: Boolean = definedExternally) {
    val layer: Int
    val persistent: Boolean
    
    fun clear(): Visual
    fun circle(pos: HasPosition, style: CircleVisualStyle = definedExternally): Visual
    fun line(pos1: HasPosition, pos2: HasPosition, style: LineVisualStyle = definedExternally): Visual
    fun poly(points: Array<HasPosition>, style: PolyVisualStyle = definedExternally): Visual
    fun rect(pos: HasPosition, w: Double, h: Double, style: RectVisualStyle = definedExternally): Visual
    fun text(text: String, pos: HasPosition, style: TextVisualStyle = definedExternally): Visual
    fun size(): Int
}

external interface CircleVisualStyle : Options {
    var radius: Double?
    var fill: Color?
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
    var lineStyle: LineStyle?
}

external interface LineVisualStyle : Options {
    var width: Double?
    var color: Color?
    var opacity: Double?
    var lineStyle: LineStyle?
}

external interface PolyVisualStyle : Options {
    var fill: Color?
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
    var lineStyle: LineStyle?
}

external interface RectVisualStyle : Options {
    var fill: Color?
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
    var lineStyle: LineStyle?
}

external interface TextVisualStyle : Options {
    var align: TextAlign?
    var backgroundColor: Color?
    var backgroundPadding: Double?
    var color: Color?
    var font: dynamic
    var opacity: Double?
    var stroke: Color?
    var strokeWidth: Double?
}
