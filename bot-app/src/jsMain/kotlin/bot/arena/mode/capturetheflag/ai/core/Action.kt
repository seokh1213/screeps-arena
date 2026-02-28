package bot.arena.mode.capturetheflag.ai.core

import screeps.bindings.ResourceConstant

/**
 * Executor 단계에서 "이번 틱에 실제로 실행할 원자 행동".
 * 기존 프로젝트의 Order/Instruction(커맨드 패턴)로 변환된다.
 */
sealed interface Action {
    data object None : Action
    data class Move(val to: Pos) : Action
    data class Attack(val targetId: String) : Action
    data class RangedAttack(val targetId: String) : Action
    data class Heal(val targetId: String) : Action
    data class RangedHeal(val targetId: String) : Action
    data class Transfer(val targetId: String, val resource: ResourceConstant) : Action
    data class Withdraw(val targetId: String, val resource: ResourceConstant) : Action
}
