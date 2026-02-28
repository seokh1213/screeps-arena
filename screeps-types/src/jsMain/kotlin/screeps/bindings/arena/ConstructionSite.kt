@file:JsModule("game/prototypes/construction-site")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class ConstructionSite : GameObject {
    val progress: Int?
    val progressTotal: Int?
    val structure: Structure?
    val my: Boolean?
    fun remove()

    companion object : Prototype<ConstructionSite>
}
