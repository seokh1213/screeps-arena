package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Constraints
import bot.arena.mode.capturetheflag.ai.core.FormationSpec
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.StackFormation
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.UnitMode
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.ATTACK
import screeps.bindings.arena.Creep
import screeps.bindings.arena.game.getRange

/**
 * 플래그 점령/유지.
 * - 한 명은 mustOccupyPos로 플래그를 밟도록 강제(가능하면 근거리 역할을 우선)
 * - 나머지는 formation 슬롯 유지
 */
class CaptureFlagTactic : Tactic {
    override val name: String = "CaptureFlag"

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        val flag = tacticContext.targetFlagId?.let(world::getFlagById)
        val flagPos: Pos = flag?.toPos() ?: tacticContext.targetPos ?: return
        val formation: FormationSpec = tacticContext.formation ?: StackFormation
        val slots = formation.computeSlots(flagPos, members, world)

        val capturer = pickCapturer(members, flagPos)

        members.forEach { creep ->
            val desired = slots[creep.id.toString()] ?: flagPos
            val constraints = if (creep == capturer) Constraints(mustOccupyPos = flagPos) else Constraints()
            outputIntents[creep.id.toString()] = UnitIntent(
                mode = UnitMode.Advance,
                desiredPos = desired,
                constraints = constraints,
            )
        }
    }

    private fun pickCapturer(members: List<Creep>, flagPos: Pos): Creep {
        val attackers = members.filter { c -> c.body.any { it.type == ATTACK && it.hits > 0 } }
        val pool = if (attackers.isNotEmpty()) attackers else members
        return pool.minBy { getRange(it, flagPos.toHasPosition()) }
    }
}

private fun screeps.bindings.arena.Flag.toPos(): Pos = Pos(x.toInt(), y.toInt())
