@file:JsModule("arena/season_2/capture_the_flag/basic/prototypes")
@file:JsNonModule

package screeps.bindings.arena.season2.capturetheflag.basic

import screeps.bindings.BodyPartConstant
import screeps.bindings.arena.GameObject
import screeps.bindings.arena.game.Prototype

typealias BodyPartType = BodyPartConstant

/**
 * A separate part of creep body
 */
abstract external class BodyPart : GameObject {
    /** The type of the body part */
    val type: BodyPartType

    companion object : Prototype<BodyPart>
}
