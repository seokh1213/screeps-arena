package bot.arena.mode.capturetheflag.model

interface StrategyDirector<T> where T : CreepRoleEnum, T : Enum<T> {
    fun decide(): StrategyPlan<T>
}
