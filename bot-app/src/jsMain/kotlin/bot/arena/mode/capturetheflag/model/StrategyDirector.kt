package bot.arena.mode.capturetheflag.model

interface StrategyDirector {
    fun plan(ctx: Context): BattlePlan
}
