@file:JsModule("game/path-finder")
@file:JsNonModule
package screeps.bindings.arena.game

import screeps.bindings.arena.HasPosition

external fun searchPath(origin: HasPosition, goal: dynamic, options: SearchPathOptions = definedExternally): SearchPathResult
external fun searchPath(origin: HasPosition, goal: Array<dynamic>, options: SearchPathOptions = definedExternally): SearchPathResult

external class CostMatrix {
    fun get(x: Int, y: Int): Int
    fun set(x: Int, y: Int, cost: Int)
    fun clone(): CostMatrix
}

external interface SearchPathOptions {
    var costMatrix: CostMatrix?
    var plainCost: Int?
    var swampCost: Int?
    var flee: Boolean?
    var maxOps: Int?
    var maxCost: Int?
    var heuristicWeight: Double?
}

external interface SearchPathResult {
    val path: Array<HasPosition>
    val ops: Int
    val cost: Int
    val incomplete: Boolean
}

external interface GoalWithRange {
    val pos: HasPosition
    val range: Int
}

