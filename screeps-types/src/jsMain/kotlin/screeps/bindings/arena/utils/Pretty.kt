package screeps.bindings.arena.utils

import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.BodyPart
import screeps.bindings.arena.Creep
import screeps.bindings.arena.SpawnCreepResult
import screeps.bindings.arena.Spawning
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.arena.game.Store
import screeps.bindings.getCapacity
import screeps.bindings.getFreeCapacity
import screeps.bindings.getUsedCapacity

fun StructureSpawn.pretty(): String = buildString {
    appendLine("StructureSpawn {")
    appendLine("  id = $id")
    appendLine("  position = ($x, $y)")
    appendLine("  hits = $hits")
    appendLine("  hitsMax = $hitsMax")
    appendLine("  my = $my")
    appendLine("  directions = [${directions.joinToString(", ")}] ")
    appendLine("  store = ${store.pretty().indentLines(2)}")
    appendLine("  spawning = ${spawning?.pretty()?.indentLines(2) ?: "<idle>"}")
    append("}")
}

fun Spawning.pretty(): String = buildString {
    appendLine("Spawning {")
    appendLine("  needTime = $needTime")
    appendLine("  remainingTime = $remainingTime")
    appendLine("  creep = ${creep.pretty().indentLines(2)}")
    append("}")
}

fun Creep.pretty(): String = buildString {
    appendLine("Creep {")
    appendLine("  id = $id")
    appendLine("  position = ($x, $y)")
    appendLine("  hits = $hits")
    appendLine("  hitsMax = $hitsMax")
    appendLine("  fatigue = $fatigue")
    appendLine("  my = $my")
    appendLine("  spawning = $spawning")
    appendLine("  store = ${store.pretty().indentLines(2)}")
    appendLine("  body = [${body.joinToString(", ") { it.pretty() }}]")
    append("}")
}

fun BodyPart.pretty(): String = "{type=$type,hits=$hits}"

fun SpawnCreepResult.pretty(): String = buildString {
    appendLine("SpawnCreepResult {")
    appendLine("  object = ${`object`?.jsonString() ?: "<null>"}")
    appendLine("  error = ${error ?: "<null>"}")
    append("}")
}

fun Store.pretty(): String = buildString {
    appendLine("Store {")
    appendLine("  capacity(energy) = ${getCapacity(RESOURCE_ENERGY)}")
    appendLine("  used(energy) = ${getUsedCapacity(RESOURCE_ENERGY)}")
    appendLine("  free(energy) = ${getFreeCapacity(RESOURCE_ENERGY)}")
    append("}")
}

fun Any?.jsonString(): String? = try {
    js("JSON").stringify(this, null, 2).unsafeCast<String?>()
} catch (_: Throwable) {
    null
}

private fun String.indentLines(spaces: Int): String {
    val lines = split("\n")
    return lines.first() + "\n" + lines.drop(1).joinToString("\n") { " ".repeat(spaces) + it }
}
