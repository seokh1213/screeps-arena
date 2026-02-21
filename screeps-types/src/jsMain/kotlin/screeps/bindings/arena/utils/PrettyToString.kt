package screeps.bindings.arena.utils

import screeps.bindings.arena.BodyPart
import screeps.bindings.arena.Creep
import screeps.bindings.arena.SpawnCreepResult
import screeps.bindings.arena.Spawning
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.arena.game.PrototypeCreep
import screeps.bindings.arena.game.PrototypeSpawning
import screeps.bindings.arena.game.PrototypeStructureSpawn
import screeps.bindings.arena.game.Store

/**
 * Installs pretty-printing via toString() for arena objects.
 *
 * Maintenance notes:
 * - Prefer prototype-level overrides for exported classes (Creep, StructureSpawn, Spawning).
 * - Use instance-level overrides only for plain-object results/nested objects
 *   (SpawnCreepResult, Store, BodyPart) that do not have a stable prototype export.
 * - Some engine objects are read-only/non-extensible, so instance binding must be best-effort.
 */
private var installedPrettyToString = false
private val prettyBoundInstances: dynamic = js("new WeakSet()")
private val prettyNestedBoundInstances: dynamic = js("new WeakSet()")

private fun isObjectLike(value: dynamic): Boolean = js(
    "(function(v){ return (typeof v === 'object' || typeof v === 'function') && v !== null; })"
)(value).unsafeCast<Boolean>()

private fun bindPrototypeToString(
    prototypeObject: dynamic,
    formatter: (dynamic) -> String
) {
    val jsBinder = js(
        "(function(proto, fmt) { proto.toString = function() { return fmt(this); }; })"
    )
    jsBinder(prototypeObject.prototype, formatter)
}

private fun bindSpawnCreepResultToString() {
    // SpawnCreepResult is returned as a plain object, so patching spawnCreep is the safest hook.
    val jsBinder = js(
        "(function(proto, decorate) {" +
            "var original = proto.spawnCreep;" +
            "proto.spawnCreep = function(body) {" +
            "  var result = original.call(this, body);" +
            "  decorate(result);" +
            "  return result;" +
            "};" +
        "})"
    )
    jsBinder(PrototypeStructureSpawn.asDynamic().prototype) { result: dynamic ->
        bindInstanceToString(result) { self ->
            self.unsafeCast<SpawnCreepResult>().pretty()
        }
    }
}

private fun bindInstanceToString(
    instance: dynamic,
    formatter: (dynamic) -> String
) {
    if (instance == null) return
    if (!isObjectLike(instance)) return
    if (prettyBoundInstances.has(instance).unsafeCast<Boolean>()) return

    try {
        instance.toString = { formatter(instance) }
        prettyBoundInstances.add(instance)
    } catch (_: dynamic) {
        // Some arena objects are non-extensible or have read-only properties.
    }
}

private fun bindNestedPrettyObjects(instance: dynamic) {
    if (instance == null) return
    if (!isObjectLike(instance)) return
    if (prettyNestedBoundInstances.has(instance).unsafeCast<Boolean>()) return
    prettyNestedBoundInstances.add(instance)

    // Nested fields are plain object/interface values in JS bindings.
    // Bind toString on each concrete instance when accessible.
    if (instance.store != null) {
        bindInstanceToString(instance.store) { self ->
            self.unsafeCast<Store>().pretty()
        }
    }
    if (instance.spawning != null) {
        bindInstanceToString(instance.spawning) { self ->
            self.unsafeCast<Spawning>().pretty()
        }
    }
    if (instance.body != null) {
        val parts = instance.body.unsafeCast<Array<dynamic>>()
        parts.forEach { part ->
            bindInstanceToString(part) { self ->
                self.unsafeCast<BodyPart>().pretty()
            }
        }
    }
}

fun installPrettyToStringOverrides() {
    if (installedPrettyToString) return
    installedPrettyToString = true

    // Add new class-level targets here when you have a PrototypeX export in arena/game.
    bindPrototypeToString(PrototypeStructureSpawn) { self ->
        bindNestedPrettyObjects(self)
        self.unsafeCast<StructureSpawn>().pretty()
    }
    bindPrototypeToString(PrototypeCreep) { self ->
        bindNestedPrettyObjects(self)
        self.unsafeCast<Creep>().pretty()
    }
    bindPrototypeToString(PrototypeSpawning) { self ->
        bindNestedPrettyObjects(self)
        self.unsafeCast<Spawning>().pretty()
    }
    bindSpawnCreepResultToString()
}
