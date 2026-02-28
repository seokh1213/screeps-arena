package bot.arena.mode.capturetheflag.ai.htn

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.CapabilityRequest
import bot.arena.mode.capturetheflag.ai.core.GroupOperation
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticAssignment
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.ATTACK
import screeps.bindings.CARRY
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag

/**
 * Hierarchical Task Network에서 재사용 가능한 primitive task 모음(초안)
 */

class SelectTwoNeutralFlagsTask(
    override val name: String = "SelectTwoNeutralFlags",
) : PrimitiveTask {
    override fun apply(world: WorldModel, blackboard: Blackboard, out: PlanBuilder) {
        val neutral = world.flags.filter { it.my == null }
        val (a, b) = neutral.take(2).let {
            it.getOrNull(0) to it.getOrNull(1)
        }
        // 이미 설정돼 있으면 유지
        if (blackboard.primaryTargetFlagId == null) blackboard.primaryTargetFlagId = a?.id?.toString()
        if (blackboard.secondaryTargetFlagId == null) blackboard.secondaryTargetFlagId = b?.id?.toString()
    }
}

class AllocateSquadTask(
    private val squadId: String,
    private val requirement: CapabilityRequest,
    private val tactic: Tactic,
    private val context: (WorldModel, Blackboard) -> TacticContext,
    override val name: String = "AllocateSquad($squadId)",
) : PrimitiveTask {

    override fun apply(world: WorldModel, blackboard: Blackboard, out: PlanBuilder) {
        val picked = pick(world, out.reservedCreepIds)
        if (picked.isEmpty()) return

        out.reservedCreepIds.addAll(picked.map { it.id.toString() })
        out.op(GroupOperation.CreateSquad(squadId = squadId, members = picked.map { it.id.toString() }))
        out.assign(TacticAssignment(squadId = squadId, tactic = tactic, context = context(world, blackboard)))
    }

    private fun pick(world: WorldModel, reserved: Set<String>): List<Creep> {
        val free = world.myCreeps.filter { it.id.toString() !in reserved }
        val picked = mutableListOf<Creep>()

        fun take(role: Role, n: Int) {
            if (n <= 0) return
            val cands = free.filter { it !in picked }.filter { it.determineRole() == role }
            picked += cands.take(n)
        }

        take(Role.WORKER, requirement.minWorker)
        take(Role.MELEE, requirement.minMelee)
        take(Role.RANGER, requirement.minRanged)
        take(Role.HEALER, requirement.minHeal)

        // 부족하면 실패
        if (picked.count { it.determineRole() == Role.WORKER } < requirement.minWorker) return emptyList()
        if (picked.count { it.determineRole() == Role.MELEE } < requirement.minMelee) return emptyList()
        if (picked.count { it.determineRole() == Role.RANGER } < requirement.minRanged) return emptyList()
        if (picked.count { it.determineRole() == Role.HEALER } < requirement.minHeal) return emptyList()

        val max = requirement.maxSize
        return if (max != null) picked.take(max) else picked
    }
}

class SetRallyPosFromFlagTask(
    private val squadId: String,
    private val flagSelector: (WorldModel, Blackboard) -> Flag?,
    override val name: String = "SetRallyPosFromFlag($squadId)",
) : PrimitiveTask {
    override fun apply(world: WorldModel, blackboard: Blackboard, out: PlanBuilder) {
        val flag = flagSelector(world, blackboard) ?: return
        // 매우 단순한 rally: 플래그에서 한 칸 아래
        blackboard.setSquadValue(squadId, "rallyPos", Pos(flag.x.toInt(), flag.y.toInt() + 1))
    }
}

private fun Creep.determineRole(): Role {
    val bodyParts = body.map { it.type }
    return when {
        bodyParts.contains(CARRY) -> Role.WORKER
        bodyParts.contains(HEAL) -> Role.HEALER
        bodyParts.contains(RANGED_ATTACK) -> Role.RANGER
        bodyParts.contains(ATTACK) -> Role.MELEE
        else -> Role.CREEP
    }
}
