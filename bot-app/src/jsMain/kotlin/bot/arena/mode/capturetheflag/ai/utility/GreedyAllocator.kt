package bot.arena.mode.capturetheflag.ai.utility

import bot.arena.mode.capturetheflag.ai.core.GroupOperation
import bot.arena.mode.capturetheflag.ai.core.StrategyPlan
import bot.arena.mode.capturetheflag.ai.core.TacticAssignment
import bot.arena.mode.capturetheflag.ai.core.WorldModel
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.arena.Creep

/**
 * 초안: 가장 단순한 Greedy 할당기.
 * - 주어진 TaskRequest들을 순서대로 처리
 * - 남은 크립 풀에서 capability를 만족하는 최소 인원을 뽑아 스쿼드 생성
 *
 * 고급 버전으로 발전시키면:
 * - stickyKey로 기존 스쿼드 유지
 * - 비용 함수(거리/피/역할 파괴)를 고려한 최적화
 */
class GreedyAllocator(
    private val roleOf: (Creep) -> Role,
    private val squadIdPrefix: String = "util",
) {
    fun allocate(world: WorldModel, tasks: List<TaskRequest>): StrategyPlan {
        val free = world.myCreeps.toMutableList()
        val ops = mutableListOf<GroupOperation>()
        val assigns = mutableListOf<TacticAssignment>()

        tasks.forEachIndexed { idx, task ->
            val picked = pickFor(task, free)
            if (picked.isEmpty()) return@forEachIndexed
            picked.forEach { free.remove(it) }

            val squadId = "$squadIdPrefix:${task.id}:$idx"
            ops += GroupOperation.CreateSquad(
                squadId = squadId,
                members = picked.map { it.id.toString() },
            )
            assigns += TacticAssignment(
                squadId = squadId,
                tactic = task.tactic,
                context = task.context,
            )
        }

        // NOTE: disband 정책은 Coordinator/Director 쪽에서 관리하는 편이 더 좋다.
        return StrategyPlan(operations = ops, assignments = assigns)
    }

    private fun pickFor(task: TaskRequest, free: List<Creep>): List<Creep> {
        val picked = mutableListOf<Creep>()

        fun take(role: Role, n: Int) {
            if (n <= 0) return
            val candidates = free.filter { it !in picked }.filter { roleOf(it) == role }
            picked += candidates.take(n)
        }

        take(Role.WORKER, task.capability.minWorker)
        take(Role.MELEE, task.capability.minMelee)
        take(Role.RANGER, task.capability.minRanged)
        take(Role.HEALER, task.capability.minHeal)

        // 최소 요구를 못 채웠으면 실패 처리(초안: 빈 리스트 반환)
        if (picked.count { roleOf(it) == Role.WORKER } < task.capability.minWorker) return emptyList()
        if (picked.count { roleOf(it) == Role.MELEE } < task.capability.minMelee) return emptyList()
        if (picked.count { roleOf(it) == Role.RANGER } < task.capability.minRanged) return emptyList()
        if (picked.count { roleOf(it) == Role.HEALER } < task.capability.minHeal) return emptyList()

        // maxSize 적용
        val maxSize = task.capability.maxSize
        return if (maxSize != null) picked.take(maxSize) else picked
    }
}
