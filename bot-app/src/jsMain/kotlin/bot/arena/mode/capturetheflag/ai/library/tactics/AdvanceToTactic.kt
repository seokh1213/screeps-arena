package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.FormationSpec
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.StackFormation
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.UnitMode
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.arena.Creep

/**
 * 목적지로 집결/전진.
 * - formation이 있으면 슬롯 기반
 * - 없으면 모든 유닛이 targetPos로
 */
class AdvanceToTactic : Tactic {
    override val name: String = "AdvanceTo"

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        val target: Pos = tacticContext.targetPos ?: return
        val formation: FormationSpec = tacticContext.formation ?: StackFormation
        val slots = formation.computeSlots(target, members, world)

        members.forEach { creep ->
            val slot = slots[creep.id.toString()] ?: target
            outputIntents[creep.id.toString()] = UnitIntent(
                mode = UnitMode.Advance,
                desiredPos = slot,
            )
        }
    }
}
