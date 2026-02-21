package bot.arena.strategy

/**
 * 전략들을 순서대로 실행하는 Composite Strategy.
 *
 * 현재 전략의 [Strategy.isDone]이 true가 되면 다음 전략으로 진행한다.
 * 모든 전략이 완료되면 [isDone]이 true가 된다.
 *
 * 예시:
 * ```kotlin
 * SequentialStrategy(listOf(
 *     SpawnWorkersStrategy(mySpawn),  // 완료되면 →
 *     BuildTowerStrategy(mySpawn)     // 다음 실행
 * ))
 * ```
 */
class SequentialStrategy(private val steps: List<Strategy>) : Strategy {
    constructor(vararg steps: Strategy) : this(steps.toList())

    private var currentIndex = 0
    private var initialized = false

    override fun initialize() {
        currentIndex = 0
        initialized = false
    }

    override fun tick() {
        val current = steps.getOrNull(currentIndex) ?: return

        if (!initialized) {
            current.initialize()
            initialized = true
        }

        current.tick()

        if (current.isDone) {
            currentIndex++
            initialized = false
        }
    }

    override val isDone: Boolean
        get() = currentIndex >= steps.size
}
