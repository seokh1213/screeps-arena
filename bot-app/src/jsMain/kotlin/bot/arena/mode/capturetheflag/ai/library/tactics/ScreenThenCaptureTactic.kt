package bot.arena.mode.capturetheflag.ai.library.tactics

import bot.arena.mode.capturetheflag.ai.core.Blackboard
import bot.arena.mode.capturetheflag.ai.core.Pos
import bot.arena.mode.capturetheflag.ai.core.TacticContext
import bot.arena.mode.capturetheflag.ai.core.UnitIntent
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.ai.library.hsm.StatefulTactic
import screeps.bindings.arena.Creep
import screeps.bindings.arena.game.getRange

/**
 * 네가 말한 "접근 -> 적 감지 시 진형 -> 점령 -> 전진" 흐름을
 * HSM(StatefulTactic) 형태로 표현한 예시.
 *
 * - 이 전술 자체는 Team B-1 같은 소규모 교전조에 잘 맞지만,
 *   인터페이스 자체는 재사용 가능.
 */
class ScreenThenCaptureTactic : StatefulTactic() {
    override val name: String = "ScreenThenCapture"

    override val initialState: String = S.Advance.name

    private enum class S { Advance, FormUp, CapturePush }

    private val advance = AdvanceToTactic()
    private val hold = HoldAreaTactic()
    private val capture = CaptureFlagTactic()

    override fun runState(
        state: String,
        squadId: String,
        members: List<Creep>,
        world: WorldModel,
        blackboard: Blackboard,
        tacticContext: TacticContext,
        outputIntents: MutableMap<String, UnitIntent>,
    ): String {
        val flag = tacticContext.targetFlagId?.let(world::getFlagById)
        val flagPos: Pos = flag?.let { Pos(it.x.toInt(), it.y.toInt()) } ?: tacticContext.targetPos ?: return state

        val minDist = members.minOfOrNull { getRange(it, flagPos.toHasPosition()) } ?: 999
        val enemyNear = world.enemiesInRange(flagPos.toHasPosition(), tacticContext.engage.enemyDetectRange).isNotEmpty()

        return when (S.valueOf(state)) {
            S.Advance -> {
                // 접근 중인데, 거의 도착했고 적이 근처면 진형
                advance.tick(squadId, members, world, blackboard, tacticContext.copy(targetPos = flagPos), outputIntents)
                if (minDist <= 3 && enemyNear) S.FormUp.name else S.Advance.name
            }

            S.FormUp -> {
                // 진형 유지하며 시간 벌기
                hold.tick(squadId, members, world, blackboard, tacticContext.copy(targetPos = flagPos), outputIntents)
                if (!enemyNear) S.CapturePush.name else S.FormUp.name
            }

            S.CapturePush -> {
                // 점령 + 전진(초안에서는 점령만)
                capture.tick(squadId, members, world, blackboard, tacticContext.copy(targetPos = flagPos), outputIntents)
                S.CapturePush.name
            }
        }
    }
}
