package screeps.bindings

import screeps.bindings.arena.game.Store

inline val <T> Constant<T>.value: T get() = this.asDynamic().unsafeCast<T>()

typealias StringConstant = Constant<String>
typealias IntConstant = Constant<Int>

fun ResourceConstant.asString(): String = this.asDynamic().unsafeCast<String>()

fun Store.getCapacity(resource: ResourceConstant?): Int? =
    getCapacity(resource?.asString())

fun Store.getUsedCapacity(resource: ResourceConstant?): Int? =
    getUsedCapacity(resource?.asString())

fun Store.getFreeCapacity(resource: ResourceConstant?): Int? =
    getFreeCapacity(resource?.asString())

