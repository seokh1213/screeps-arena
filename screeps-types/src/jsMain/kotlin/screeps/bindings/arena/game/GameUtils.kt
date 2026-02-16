@file:JsModule("game/utils")
@file:JsNonModule

package screeps.bindings.arena.game

import screeps.bindings.DirectionConstant
import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.ConstructionSite
import screeps.bindings.arena.GameObject
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.Structure

external fun <T> getObjectsByPrototype(prototype: Prototype<T>): Array<T>
external fun getHeapStatistics(): HeapStatistics
external fun getCpuTime(): Long
external fun getDirection(dx: Int, dy: Int): DirectionConstant
external fun getObjects(): Array<GameObject>
external fun getRange(a: HasPosition, b: HasPosition): Int
external fun <T : HasPosition> findClosestByPath(
    fromPos: HasPosition,
    positions: Array<T>,
    options: FindPathOptions = definedExternally
): T?

external fun <T : HasPosition> findClosestByRange(fromPos: HasPosition, positions: Array<T>): T?
external fun <T : HasPosition> findInRange(fromPos: HasPosition, positions: Array<T>, range: Int): Array<T>
external fun findPath(
    fromPos: HasPosition,
    toPos: HasPosition,
    options: FindPathOptions = definedExternally
): Array<HasPosition>

external fun getObjectById(id: String): GameObject?
external fun getTerrainAt(pos: HasPosition): Int
external fun getTicks(): Int
external fun <T : Structure> createConstructionSite(
    pos: HasPosition,
    structurePrototype: Prototype<T>
): CreateConstructionSiteResult

external interface Prototype<T>

external interface HeapStatistics {
    @JsName("externally_allocated_size")
    val externallyAllocatedSize: Long
    val totalHeapSize: Long
    val totalHeapSizeExecutable: Long
    val totalPhysicalSize: Long
    val totalAvailableSize: Long
    val usedHeapSize: Long
    val heapSizeLimit: Long
    val mallocedMemory: Long
    val peakMallocedMemory: Long
    val doesZapGarbage: Int
    val numberOfNativeContexts: Int
    val numberOfDetachedContexts: Int
}

external interface FindPathOptions {
    var costMatrix: CostMatrix?
    var plainCost: Int?
    var swampCost: Int?
    var flee: Boolean?
    var maxOps: Int?
    var maxCost: Int?
    var heuristicWeight: Double?
    var ignore: Array<GameObject>?
}

external interface CreateConstructionSiteResult {
    val `object`: ConstructionSite?
    val error: ScreepsReturnCode?
}
