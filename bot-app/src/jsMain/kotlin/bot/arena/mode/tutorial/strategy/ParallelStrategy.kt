package bot.arena.mode.tutorial.strategy

/**
 * 여러 전략을 동시에(같은 틱에) 실행하는 Composite Strategy.
 *
 * 모든 전략의 [Strategy.isDone]이 true가 되면 [isDone]이 true가 된다.
 *
 * 예시:
 * ```kotlin
 * ParallelStrategy(listOf(
 *     HarvestStrategy(mySpawn),   // 동시 실행
 *     DefendBaseStrategy(mySpawn)
 * ))
 * ```
 */
class ParallelStrategy(private val all: List<Strategy>) : Strategy {
    constructor(vararg strategies: Strategy) : this(strategies.toList())

    override fun initialize() = all.forEach { it.initialize() }
    override fun tick() = all.forEach { it.tick() }
    override val isDone: Boolean get() = all.all { it.isDone }
}
