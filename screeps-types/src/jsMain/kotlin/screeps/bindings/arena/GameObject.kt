package screeps.bindings.arena

import screeps.bindings.arena.game.FindPathOptions

external interface GameObject : HasPosition {

    val exists: Boolean
    val id: dynamic // number | string
    val ticksToDecay: Int?
    override var x: Double
    override var y: Double
    val effects: Array<Effect>?

    fun <T : HasPosition> findClosestByPath(
        positions: Array<T>,
        options: FindPathOptions = definedExternally
    ): T?

    fun <T : HasPosition> findClosestByRange(
        positions: Array<T>
    ): T?

    fun <T : HasPosition> findInRange(
        positions: Array<T>,
        range: Int
    ): Array<T>

    fun findPathTo(pos: HasPosition, options: FindPathOptions = definedExternally): Array<HasPosition>

    fun getRangeTo(pos: HasPosition): Int

}

external interface Effect {
    val effectType: String
    val data: EffectData
}

external interface EffectData {
    val multiplier: Double
}
