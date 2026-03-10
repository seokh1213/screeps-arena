@file:JsModule("arena/season_2/construct_and_control/basic/prototypes")
@file:JsNonModule

package screeps.bindings.arena.season2.constructandcontrol.basic

import screeps.bindings.EffectConstant
import screeps.bindings.arena.GameObject
import screeps.bindings.arena.game.Prototype

typealias AreaEffectType = EffectConstant

/**
 * An object that applies an effect of the specified type to all creeps at the same tile.
 */
abstract external class AreaEffect : GameObject {
    val effect: AreaEffectType

    companion object : Prototype<AreaEffect>
}

/**
 * An object that provides a construction boost effect to the creep that steps onto this object.
 */
abstract external class ConstructionBoost : GameObject {
    companion object : Prototype<ConstructionBoost>
}

/**
 * A structure that needs to be built to win the match.
 */
abstract external class StructureGoal : GameObject {
    companion object : Prototype<StructureGoal>
}
