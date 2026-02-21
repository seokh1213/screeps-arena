package bot.arena.strategy

/**
 * 게임 전략의 기본 인터페이스.
 *
 * 생명주기:
 * 1. [initialize] - 전략 시작 시 한 번 호출 (선택적 오버라이드)
 * 2. [tick]       - 매 게임 틱마다 호출. 구현체 내부에서 initialize를 자동 처리.
 * 3. [isDone]     - true를 반환하면 전략 완료로 간주 (기본: false = 무한 실행)
 */
interface Strategy {
    fun initialize() {}
    fun tick()
    val isDone: Boolean get() = false
}

/**
 * 소환(spawn) 전용 전략 추상 클래스.
 *
 * - [tick]은 final로 봉인 — 서브클래스에서 오버라이드할 수 없다.
 * - 첫 번째 [tick] 호출 시 [initialize]를 자동으로 한 번 실행한다.
 * - 서브클래스는 [spawn] 만 구현하면 된다.
 * - [isDone] 기본값은 false. 소환 완료 조건이 있으면 오버라이드.
 *
 * ※ Kotlin 인터페이스에서는 final을 쓸 수 없으므로 abstract class를 사용.
 */
abstract class SpawnStrategy : Strategy {
    private var initialized = false

    abstract fun spawn()

    final override fun tick() {
        if (!initialized) {
            initialize()
            initialized = true
        }
        if (!isDone) spawn()
    }

    fun reset() { initialized = false }
}

/**
 * Creep 행동 전용 전략 추상 클래스. Worker, Attacker 등 모든 role에 공통 사용.
 *
 * - [tick]은 final로 봉인 — 서브클래스에서 오버라이드할 수 없다.
 * - 첫 번째 [tick] 호출 시 [initialize]를 자동으로 한 번 실행한다.
 * - 서브클래스는 [behave] 만 구현하면 된다.
 * - [isDone] 기본값은 false (무한 실행). 필요 시 오버라이드.
 */
abstract class BehaviorStrategy : Strategy {
    private var initialized = false

    abstract fun behave()

    final override fun tick() {
        if (!initialized) {
            initialize()
            initialized = true
        }
        if (!isDone) behave()
    }

    fun reset() { initialized = false }
}