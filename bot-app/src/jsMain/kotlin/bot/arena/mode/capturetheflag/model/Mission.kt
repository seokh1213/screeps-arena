package bot.arena.mode.capturetheflag.model

import bot.arena.mode.capturetheflag.behavior.BehaviorFactory
import bot.arena.mode.capturetheflag.behavior.common.isHealer
import bot.arena.mode.capturetheflag.behavior.common.isMelee
import bot.arena.mode.capturetheflag.behavior.common.isRanger
import bot.arena.mode.capturetheflag.behavior.common.isWorker
import screeps.bindings.arena.Creep

// --- Core Types ---

enum class CreepRole {
    MELEE, RANGER, HEALER, WORKER;

    fun matches(creep: Creep): Boolean = when (this) {
        MELEE -> creep.isMelee()
        RANGER -> creep.isRanger()
        HEALER -> creep.isHealer()
        WORKER -> creep.isWorker()
    }
}

data class RoleSpec(
    val role: CreepRole,
    val min: Int,
    val max: Int = min,
)

data class SquadAssignment(
    val squadId: String,
    val creeps: List<Creep>,
    val behaviorFactory: BehaviorFactory,
)

data class TeamAssignment(
    val teamId: String,
    val squads: List<SquadAssignment>,
)

data class BattlePlan(
    val teams: List<TeamAssignment>,
) {
    val allSquads: List<SquadAssignment>
        get() = teams.flatMap { it.squads }
}

// --- DSL Builders ---

@DslMarker
annotation class BattlePlanDsl

@BattlePlanDsl
class BattlePlanBuilder(private val ctx: Context) {
    private val teamBuilders = mutableListOf<TeamBuilder>()

    fun team(id: String, block: TeamBuilder.() -> Unit) {
        teamBuilders += TeamBuilder(id).apply(block)
    }

    fun build(): BattlePlan {
        val pool = ctx.myCreeps.filter { it.exists }.toMutableList()
        val teams = teamBuilders.mapNotNull { teamBuilder ->
            teamBuilder.squadBuilders
                .mapNotNull { allocateSquad(it, pool) }
                .takeIf { it.isNotEmpty() }
                ?.let { TeamAssignment(teamBuilder.id, it) }
        }

        return BattlePlan(teams)
    }

    private fun allocateSquad(
        builder: SquadBuilder,
        pool: MutableList<Creep>,
    ): SquadAssignment? {
        val assigned = builder.roleSpecs.flatMap { spec ->
            pool.filter { spec.role.matches(it) }
                .take(spec.max)
                .also { selected ->
                    pool.removeAll(selected.toSet())
                }
        }

        val meetsMinimum = builder.roleSpecs.all { spec ->
            assigned.count(spec.role::matches) >= spec.min
        }

        if (!meetsMinimum) {
            // min 미달 → 해체, 크립을 풀에 반환
            pool += assigned
            return null
        }

        val factory = builder.behaviorFactory
            ?: error("Squad '${builder.id}' has no behavior defined")

        return SquadAssignment(builder.id, assigned, factory)
    }
}

@BattlePlanDsl
class TeamBuilder(val id: String) {
    internal val squadBuilders = mutableListOf<SquadBuilder>()

    fun squad(id: String, block: SquadBuilder.() -> Unit) {
        squadBuilders += SquadBuilder(id).apply(block)
    }
}

@BattlePlanDsl
class SquadBuilder(val id: String) {
    internal val roleSpecs = mutableListOf<RoleSpec>()
    internal var behaviorFactory: BehaviorFactory? = null

    fun role(role: CreepRole, min: Int, max: Int = min) {
        roleSpecs += RoleSpec(role, min, max)
    }

    fun behavior(factory: BehaviorFactory) {
        behaviorFactory = factory
    }
}

fun battlePlan(ctx: Context, block: BattlePlanBuilder.() -> Unit): BattlePlan =
    BattlePlanBuilder(ctx).apply(block).build()
