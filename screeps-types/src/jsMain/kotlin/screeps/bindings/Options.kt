package screeps.bindings

import screeps.interop.jsObject


/**
 * Indicates type is an external interface with only *mutable and nullable* properties.
 * Thus it can be safely instantiated by [screeps.interop.jsObject]
 *
 * We provide the function [options] to create such objects
 */
external interface Options

fun <T : Options> options(init: T.() -> Unit): T = jsObject(init).unsafeCast<T>()

external interface FilterOption<T> : Options {
    var filter: ((T) -> Boolean)?
}