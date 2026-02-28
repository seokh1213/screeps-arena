package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.Tactic
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.UnitMode
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import screeps.bindings.arena.Creep
import screeps.bindings.arena.game.getRange

/**
 * 타워(들) 충전.
 * - tacticContext.targetTowerIds가 있으면 그 중 하나를 선택
 * - 없으면 targetPos 주변의 타워 중 가장 가까운 것을 선택
 *
 * 실제 transfer/withdraw 규칙은 WorkerPolicy가 처리.
 */
class ChargeTowersTactic : Tactic {
    override val name: String = "ChargeTowers"

    override fun tick(
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ) {
        val targetTowerId = pickTowerId(members, world, tacticContext)
        val fallbackPos: Pos? = tacticContext.targetPos

        members.forEach { creep ->
            outputIntents[creep.id.toString()] = UnitIntent(
                mode = UnitMode.Support,
                desiredPos = fallbackPos,
                focusTargetId = targetTowerId,
            )
        }
    }

    private fun pickTowerId(members: List<Creep>, world: WorldModel, tacticContext: TacticContext): String? {
        tacticContext.targetTowerIds.firstOrNull()?.let { return it }
        val anchor = tacticContext.targetPos ?: return null
        val towers = world.towers
        val near = towers.minByOrNull { getRange(anchor.toHasPosition(), it) }
        return near?.id?.toString()
    }
}
