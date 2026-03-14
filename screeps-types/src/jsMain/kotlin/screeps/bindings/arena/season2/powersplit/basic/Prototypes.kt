@file:JsModule("arena/season_2/power_split/basic/prototypes")
@file:JsNonModule

package screeps.bindings.arena.season2.powersplit.basic

import screeps.bindings.BodyPartConstant
import screeps.bindings.arena.Flag
import screeps.bindings.arena.game.Prototype

typealias BodyPartType = BodyPartConstant

/**
 * An object that applies an effect of the specified type to all creeps belonging to the player who captured it.
 */
abstract external class BonusFlag : Flag {
    /** The affected bodypart type */
    val bonusType: BodyPartType

    companion object : Prototype<BonusFlag>
}
