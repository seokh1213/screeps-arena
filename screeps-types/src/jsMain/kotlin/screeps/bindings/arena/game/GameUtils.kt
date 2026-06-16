@file:JsModule("game/utils")
@file:JsNonModule

package screeps.bindings.arena.game

import screeps.bindings.DirectionConstant
import screeps.bindings.Options
import screeps.bindings.ScreepsReturnCode
import screeps.bindings.arena.ConstructionSite
import screeps.bindings.arena.GameObject
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.Structure

external fun <T> getObjectsByPrototype(prototype: Prototype<T>): Array<T>
external fun getHeapStatistics(): HeapInfo
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

typealias Direction = DirectionConstant
typealias Terrain = Int
typealias DoesZapCodeSpaceFlag = Int
typealias HeapInfo = HeapStatistics

external interface HeapStatistics {
    @JsName("total_heap_size")
    val totalHeapSize: Long
    @JsName("total_heap_size_executable")
    val totalHeapSizeExecutable: Long
    @JsName("total_physical_size")
    val totalPhysicalSize: Long
    @JsName("total_available_size")
    val totalAvailableSize: Long
    @JsName("used_heap_size")
    val usedHeapSize: Long
    @JsName("heap_size_limit")
    val heapSizeLimit: Long
    @JsName("malloced_memory")
    val mallocedMemory: Long
    @JsName("peak_malloced_memory")
    val peakMallocedMemory: Long
    @JsName("does_zap_garbage")
    val doesZapGarbage: DoesZapCodeSpaceFlag
    @JsName("number_of_native_contexts")
    val numberOfNativeContexts: Int
    @JsName("number_of_detached_contexts")
    val numberOfDetachedContexts: Int
    @JsName("externally_allocated_size")
    val externallyAllocatedSize: Long
}

external interface FindPathOptions : SearchPathOptions {
    var ignore: Array<GameObject>?
}

external interface CreateConstructionSiteResult {
    val `object`: ConstructionSite?
    val error: ScreepsReturnCode?
}
