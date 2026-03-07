package bot.arena.mode.capturetheflag.model

interface StrategyDirector {
    fun decide(): StrategyPlan
}