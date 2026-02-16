@file:Suppress("UNCHECKED_CAST", "unused")

package screeps.support

import kotlin.js.Json

fun <T> jsonToMap(json: Json): Map<String, T> {
    val map: MutableMap<String, T> = linkedMapOf()
    for (key in js("Object").keys(json)) {
        map[key] = json[key as String] as T
    }
    return map
}
