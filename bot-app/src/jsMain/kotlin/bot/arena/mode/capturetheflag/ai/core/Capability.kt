package bot.arena.mode.capturetheflag.ai.core

/**
 * 그룹 배치를 위한 "능력 요구사항".
 * Team A/B에 핏하지 않게, 어떤 목표든 이 요구사항만으로 인력을 뽑을 수 있게 한다.
 */
data class CapabilityRequest(
    val minMelee: Int = 0,
    val minRanged: Int = 0,
    val minHeal: Int = 0,
    val minWorker: Int = 0,
    val maxSize: Int? = null,
)
