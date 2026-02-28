package bot.arena.mode.tutorial.strategy

/**
 * 조건(context)에 따라 실행할 전략을 동적으로 선택하는 Composite Strategy.
 *
 * - [branches] 목록을 위에서부터 평가해 조건이 true인 첫 번째 전략을 실행한다.
 * - 조건 변화로 전략이 전환될 때 새 전략의 [Strategy.initialize]가 자동으로 호출된다.
 * - [isDone]은 항상 false (Selector 자체는 계속 실행).
 *
 * 예시:
 * ```kotlin
 * SelectorStrategy(listOf(
 *     { mySpawn.store.getUsedCapacity(RESOURCE_ENERGY) < 300 } to HarvestStrategy(mySpawn),
 *     { true }                                                  to AttackStrategy(mySpawn)
 * ))
 * ```
 */
class SelectorStrategy(
    private val branches: List<Pair<() -> Boolean, Strategy>>
) : Strategy {
    private var active: Strategy? = null

    override fun initialize() {
        active = null
    }

    override fun tick() {
        val selected = branches.firstOrNull { (condition, _) -> condition() }?.second

        if (selected !== active) {
            selected?.initialize()
            active = selected
        }

        active?.tick()
    }

    // Selector 자체는 항상 실행 중 (외부에서 별도로 종료 조건을 wrapping하면 됨)
    override val isDone: Boolean get() = false
}
