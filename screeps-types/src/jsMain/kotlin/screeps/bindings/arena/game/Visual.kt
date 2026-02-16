package screeps.bindings.arena.game

import screeps.bindings.arena.HasPosition

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

external interface CircleVisualStyle {
    var radius: Double?
    var fill: String?
    var opacity: Double?
    var stroke: String?
    var strokeWidth: Double?
    var lineStyle: String? // "dashed" | "dotted" | undefined
}

external interface LineVisualStyle {
    var width: Double?
    var color: String?
    var opacity: Double?
    var lineStyle: String? // "dashed" | "dotted" | undefined
}

external interface PolyVisualStyle {
    var fill: String?
    var opacity: Double?
    var stroke: String?
    var strokeWidth: Double?
    var lineStyle: String? // "dashed" | "dotted" | undefined
}

external interface RectVisualStyle {
    var fill: String?
    var opacity: Double?
    var stroke: String?
    var strokeWidth: Double?
    var lineStyle: String? // "dashed" | "dotted" | undefined
}

external interface TextVisualStyle {
    var align: String? // "center" | "left" | "right"
    var backgroundColor: String?
    var backgroundPadding: Double?
    var color: String?
    var font: dynamic // number | string
    var opacity: Double?
    var stroke: String?
    var strokeWidth: Double?
}
