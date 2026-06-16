package screeps.bindings.arena.game

import screeps.bindings.ResourceConstant

operator fun Store.get(resource: String): Int? =
    asDynamic()[resource].unsafeCast<Int?>()

operator fun Store.get(resource: ResourceConstant): Int? =
    get(resource.asDynamic().unsafeCast<String>())
