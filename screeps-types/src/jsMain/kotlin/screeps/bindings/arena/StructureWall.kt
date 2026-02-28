@file:JsModule("game/prototypes/wall")
@file:JsNonModule

package screeps.bindings.arena

import screeps.bindings.arena.game.Prototype

abstract external class StructureWall : Structure {
    companion object : Prototype<StructureWall>
}
