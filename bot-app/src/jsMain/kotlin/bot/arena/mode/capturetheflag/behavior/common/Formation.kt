package bot.arena.mode.capturetheflag.behavior.common

import bot.arena.mode.capturetheflag.model.CreepRole
import screeps.bindings.arena.Creep
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.game.getRange
import kotlin.math.sign

/**
 * 포메이션 오프셋 (중심 기준, 적 방향이 +Y 방향일 때의 상대 좌표)
 * offsetForward: 적 방향으로의 거리 (양수 = 전방, 음수 = 후방)
 * offsetRight: 오른쪽 방향 거리 (양수 = 오른쪽, 음수 = 왼쪽)
 */
data class FormationSlot(
    val role: CreepRole,
    val index: Int,
    val offsetForward: Int,
    val offsetRight: Int,
)

data class Formation(
    val slots: List<FormationSlot>,
) {
    /**
     * 중심점과 적 방향 기준으로 각 슬롯의 실제 월드 좌표를 계산한다.
     *
     * @param center 포메이션 중심점
     * @param facingToward 적(또는 목표) 방향 위치
     * @return 슬롯별 월드 좌표 리스트 (FormationSlot과 1:1 대응)
     */
    fun resolve(center: HasPosition, facingToward: HasPosition): List<ResolvedSlot> {
        val dx = (facingToward.x - center.x).sign
        val dy = (facingToward.y - center.y).sign

        // dx, dy가 0이면 기본 방향 (위쪽)
        val fdx = if (dx == 0.0 && dy == 0.0) 0.0 else dx
        val fdy = if (dx == 0.0 && dy == 0.0) -1.0 else dy

        // forward 방향 = (fdx, fdy), right 방향 = (fdy, -fdx) (시계 90도 회전)
        val rdx = fdy
        val rdy = -fdx

        return slots.map { slot ->
            val worldX = center.x + fdx * slot.offsetForward + rdx * slot.offsetRight
            val worldY = center.y + fdy * slot.offsetForward + rdy * slot.offsetRight
            ResolvedSlot(slot, worldX, worldY)
        }
    }
}

data class ResolvedSlot(
    val slot: FormationSlot,
    override var x: Double,
    override var y: Double,
) : HasPosition

private data class IndexedAssignment(
    val slotIndex: Int,
    val creep: Creep,
    val slot: ResolvedSlot,
)

private fun Creep.roleOrNull(): CreepRole? = when {
    isMelee() -> CreepRole.MELEE
    isRanger() -> CreepRole.RANGER
    isHealer() -> CreepRole.HEALER
    isWorker() -> CreepRole.WORKER
    else -> null
}

// --- 프리셋 포메이션 ---

/**
 * ㄴ자 (니은) 포메이션 - Team B 소규모 전투용
 *
 *  적 방향 →
 *
 *  [M]         ← 근거리 (최전방)
 *       [R]    ← 레인저 (뒤 + 옆)
 *       [H]    ← 힐러 (레인저 옆)
 */
val NIEUN_FORMATION = Formation(
    listOf(
        FormationSlot(CreepRole.MELEE, 0, offsetForward = 2, offsetRight = 0),
        FormationSlot(CreepRole.RANGER, 0, offsetForward = 0, offsetRight = 1),
        FormationSlot(CreepRole.HEALER, 0, offsetForward = 0, offsetRight = -1),
    )
)

/**
 * 라인 포메이션 - Team A 대규모 전투용
 *
 * 적 방향 →
 *
 *  [M][M][M]       ← 근거리 전선
 *  [R][R][R]       ← 레인저 두번째
 *  [H][H][H]       ← 힐러 세번째
 */
val LINE_FORMATION = Formation(
    listOf(
        // 근거리 전선
        FormationSlot(CreepRole.MELEE, 0, offsetForward = 2, offsetRight = -1),
        FormationSlot(CreepRole.MELEE, 1, offsetForward = 2, offsetRight = 0),
        FormationSlot(CreepRole.MELEE, 2, offsetForward = 2, offsetRight = 1),
        // 레인저 두번째
        FormationSlot(CreepRole.RANGER, 0, offsetForward = 0, offsetRight = -1),
        FormationSlot(CreepRole.RANGER, 1, offsetForward = 0, offsetRight = 0),
        FormationSlot(CreepRole.RANGER, 2, offsetForward = 0, offsetRight = 1),
        // 힐러 세번째
        FormationSlot(CreepRole.HEALER, 0, offsetForward = -2, offsetRight = -1),
        FormationSlot(CreepRole.HEALER, 1, offsetForward = -2, offsetRight = 0),
        FormationSlot(CreepRole.HEALER, 2, offsetForward = -2, offsetRight = 1),
    )
)

/**
 * 수비 진형 - 밀집 대형
 *
 *    [M][M][M]
 *  [H][R][R][R][H]
 *      [H]
 */
val DEFENSIVE_FORMATION = Formation(
    listOf(
        FormationSlot(CreepRole.MELEE, 0, offsetForward = 1, offsetRight = -1),
        FormationSlot(CreepRole.MELEE, 1, offsetForward = 1, offsetRight = 0),
        FormationSlot(CreepRole.MELEE, 2, offsetForward = 1, offsetRight = 1),
        FormationSlot(CreepRole.RANGER, 0, offsetForward = 0, offsetRight = -1),
        FormationSlot(CreepRole.RANGER, 1, offsetForward = 0, offsetRight = 0),
        FormationSlot(CreepRole.RANGER, 2, offsetForward = 0, offsetRight = 1),
        FormationSlot(CreepRole.HEALER, 0, offsetForward = 0, offsetRight = -2),
        FormationSlot(CreepRole.HEALER, 1, offsetForward = -1, offsetRight = 0),
        FormationSlot(CreepRole.HEALER, 2, offsetForward = 0, offsetRight = 2),
    )
)

// --- 유틸 함수 ---

/**
 * 포메이션 슬롯에 크립을 매칭한다.
 * 역할별로 index 순서대로 배정. 남는 크립은 가장 가까운 빈 슬롯에 배정.
 */
fun Formation.assignCreeps(
    creeps: List<Creep>,
    center: HasPosition,
    facingToward: HasPosition,
): List<Pair<Creep, ResolvedSlot>> {
    val resolved = resolve(center, facingToward)

    // 1차: 역할 매칭
    val roleCreeps = creeps
        .mapNotNull { creep -> creep.roleOrNull()?.let { it to creep } }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second },
        )

    val primaryAssignments = resolved.mapIndexedNotNull { index, slot ->
        roleCreeps[slot.slot.role]
            ?.getOrNull(slot.slot.index)
            ?.let { creep -> IndexedAssignment(index, creep, slot) }
    }

    // 2차: 남는 크립은 가장 가까운 빈 슬롯으로
    val usedCreepIds = primaryAssignments.map { it.creep.id.toString() }.toSet()
    val usedSlotIndexes = primaryAssignments.map { it.slotIndex }.toSet()
    val remainingSlots = resolved.filterIndexed { index, _ -> index !in usedSlotIndexes }

    val (fallbackAssignments, _) = creeps
        .filterNot { it.id.toString() in usedCreepIds }
        .fold(emptyList<Pair<Creep, ResolvedSlot>>() to remainingSlots) { (assignments, availableSlots), creep ->
            val nearestSlot = availableSlots.minByOrNull { slot -> getRange(creep, slot) }
                ?: return@fold assignments to availableSlots

            (assignments + (creep to nearestSlot)) to (availableSlots - nearestSlot)
        }

    return primaryAssignments.map { it.creep to it.slot } + fallbackAssignments
}
