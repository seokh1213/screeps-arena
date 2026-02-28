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
 * 특정 지점을 기준으로 버티기(시간 끌기/지역 통제).
 * - 적이 감지되면 Hold, 아니면 Advance(자리 잡기)
 */
class HoldAreaTactic : Tactic {
    override val name: String = "HoldArea"

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

        val enemyNear = world.enemiesInRange(target.toHasPosition(), tacticContext.engage.enemyDetectRange).isNotEmpty()
        val mode = if (enemyNear) UnitMode.Hold else UnitMode.Advance

        members.forEach { creep ->
            val slot = slots[creep.id.toString()] ?: target
            outputIntents[creep.id.toString()] = UnitIntent(
                mode = mode,
                desiredPos = slot,
            )
        }
    }
}
