@file:JsModule("game/prototypes/store")
@file:JsNonModule
package screeps.bindings.arena.game

external interface Store {
    fun getCapacity(resource: String? = definedExternally): Int?
    fun getUsedCapacity(resource: String? = definedExternally): Int?
    fun getFreeCapacity(resource: String? = definedExternally): Int?
}

