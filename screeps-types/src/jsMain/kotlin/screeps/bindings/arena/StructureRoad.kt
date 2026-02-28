@file:JsModule("game/prototypes/road")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class StructureRoad : Structure {
    companion object : Prototype<StructureRoad>
}
