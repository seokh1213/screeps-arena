package bot.arena.mode.capturetheflag.ai.core

import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.ATTACK
import screeps.bindings.CARRY
import screeps.bindings.HEAL
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.arena.Creep

/**
 * Body 구성으로 Creep 역할을 추론한다.
 * - Memory에 역할이 없을 때 fallback으로 사용
 * - HTN/Hybrid/Overmind 전역에서 동일 규칙을 공유
 */
fun Creep.determineRole(): Role {
    val bodyParts = body.map { it.type }
    return when {
        bodyParts.contains(CARRY) -> Role.WORKER
        bodyParts.contains(HEAL) -> Role.HEALER
        bodyParts.contains(RANGED_ATTACK) -> Role.RANGER
        bodyParts.contains(ATTACK) -> Role.MELEE
        else -> Role.CREEP
    }
}
