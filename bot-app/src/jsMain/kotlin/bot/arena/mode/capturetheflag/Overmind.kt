package bot.arena.mode.capturetheflag

import bot.arena.mode.capturetheflag.memory.Memory
import bot.arena.mode.capturetheflag.model.Context
import bot.arena.mode.capturetheflag.model.CreepContext
import bot.arena.mode.capturetheflag.model.Order
import bot.arena.mode.capturetheflag.model.Phase
import bot.arena.mode.capturetheflag.model.Role
import screeps.bindings.ATTACK
import screeps.bindings.CARRY
import screeps.bindings.HEAL
import screeps.bindings.MOVE
import screeps.bindings.RANGED_ATTACK
import screeps.bindings.RESOURCE_ENERGY
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.GameObject
import screeps.bindings.arena.HasPosition
import screeps.bindings.arena.Source
import screeps.bindings.arena.StructureContainer
import screeps.bindings.arena.StructureTower
import screeps.bindings.arena.game.getObjectsByPrototype
import screeps.bindings.arena.season2.capturetheflag.basic.BodyPart
import kotlin.js.unsafeCast

class Overmind {
    private val memory = Memory()

    private var homeFlagId: String? = null
    private var enemyHomeFlagId: String? = null
    private var homeTowerId: String? = null
    private var homeContainerId: String? = null

    init {
        updateMemory()
    }

    fun commandOrder(): List<Order<*>> {
        updateMemory()
        return makeOrders()
    }

    private fun updateMemory() {
        val context = getCurrentContext()
        discoverHomeAnchors(context)
        assignCreepRole(context)
        memory.creepMemory.retainAll(context.myCreeps)
        memory.updateState(evaluatePhase(context), context)
    }

    private fun getCurrentContext() = Context(
        creeps = getObjectsByPrototype(Creep),
        flags = getObjectsByPrototype(Flag),
        bodyPartItems = getObjectsByPrototype(BodyPart),
        towers = getObjectsByPrototype(StructureTower),
        containers = getObjectsByPrototype(StructureContainer),
        sources = getObjectsByPrototype(Source),
    )

    private fun evaluatePhase(context: Context): Phase {
        if (context.myCreeps.isEmpty()) {
            return Phase.START
        }

        val hasNearbyEnemies = context.myCreeps.any { creep ->
            context.enemyCreeps.any { enemy -> creep.getRangeTo(enemy) <= 5 }
        }

        if (hasNearbyEnemies) {
            return Phase.IN_BATTLE
        }

        if (context.neutralFlags.isNotEmpty()) {
            return Phase.INITIAL
        }

        return Phase.CONQUERING
    }

    private fun makeOrders(): List<Order<*>> {
        val context = memory.currentContext
        val homeFlag = resolveFlag(context, homeFlagId)
        val enemyHomeFlag = resolveFlag(context, enemyHomeFlagId)
        val homeTower = resolveTower(context, homeTowerId) ?: selectHomeTower(context, homeFlag)
        val homeContainer = resolveContainer(context, homeContainerId) ?: selectHomeContainer(context, homeFlag, homeTower)
        val directHomeThreats = selectHomeThreats(context, homeFlag)
        val incomingHomeThreats = selectIncomingHomeThreats(context, homeFlag)
        val homeThreats = (directHomeThreats + incomingHomeThreats)
            .distinctBy { it.id.toString() }
            .sortedBy { enemy -> homeFlag?.let { enemy.getRangeTo(it) } ?: Int.MAX_VALUE }
        val homeDefenderIds = selectHomeDefenderIds(context, homeFlag, directHomeThreats, incomingHomeThreats)
        val homeWorkerId = selectHomeWorkerId(context, homeFlag, homeTower)
        val objectiveTarget = selectObjectiveTarget(context, homeFlag, enemyHomeFlag, homeThreats)
        val guardTarget = selectGuardTarget(context, homeFlag, homeThreats)
        val defenseTarget = homeThreats.firstOrNull()
        val defenseAnchor = homeFlag ?: defenseTarget ?: guardTarget

        val creepOrders = context.myCreeps.map { creep ->
            val role = roleOf(creep)
            val defendHome = creep.id.toString() in homeDefenderIds
            val isHomeWorker = role == Role.WORKER && creep.id.toString() == homeWorkerId
            val creepObjective = if (defendHome) defenseAnchor else objectiveTarget
            val creepGuard = if (defendHome || isHomeWorker) defenseAnchor ?: guardTarget else guardTarget

            when (role) {
                Role.WORKER -> {
                    if (isHomeWorker) {
                        orderFor(creep) {
                            homeWorkerStep(
                                creep = this,
                                context = context,
                                homeFlag = homeFlag,
                                homeTower = homeTower,
                                homeContainer = homeContainer,
                                homeThreats = homeThreats,
                            )
                        }
                    } else {
                        orderFor(creep) { workerStep(this, context, creepObjective, creepGuard) }
                    }
                }

                Role.HEALER -> orderFor(creep) { healerStep(this, context, creepObjective, creepGuard) }
                Role.RANGER -> orderFor(creep) { rangerStep(this, context, creepObjective, creepGuard) }
                Role.MELEE, Role.CREEP -> orderFor(creep) { meleeStep(this, context, creepObjective, creepGuard) }
            }
        }

        val towerOrders = context.myTowers.map { tower ->
            orderFor(tower) {
                towerStep(
                    tower = this,
                    context = context,
                    homeFlag = homeFlag,
                )
            }
        }

        return creepOrders + towerOrders
    }

    private fun discoverHomeAnchors(context: Context) {
        if (homeFlagId == null) {
            homeFlagId = context.myFlags.minByOrNull { flag ->
                distanceFromGroup(context.myCreeps, flag)
            }?.id?.toString()
        }

        if (enemyHomeFlagId == null) {
            enemyHomeFlagId = context.enemyFlags.minByOrNull { flag ->
                distanceFromGroup(context.enemyCreeps, flag)
            }?.id?.toString()
        }

        val homeFlag = resolveFlag(context, homeFlagId)
        homeTowerId = (resolveTower(context, homeTowerId) ?: selectHomeTower(context, homeFlag))?.id?.toString()
        val homeTower = resolveTower(context, homeTowerId)
        homeContainerId = (resolveContainer(context, homeContainerId)
            ?: selectHomeContainer(context, homeFlag, homeTower))?.id?.toString()
    }

    private fun assignCreepRole(context: Context) {
        context.myCreeps.forEach { creep ->
            val creepContext = memory.creepMemory[creep]
            if (creepContext?.id?.isNotEmpty() == true) {
                return@forEach
            }

            memory.creepMemory[creep] = CreepContext(
                id = creep.id.toString(),
                role = creep.determineRole(),
            )
        }
    }

    private fun homeWorkerStep(
        creep: Creep,
        context: Context,
        homeFlag: Flag?,
        homeTower: StructureTower?,
        homeContainer: StructureContainer?,
        homeThreats: List<Creep>,
    ) {
        if (homeFlag != null && homeThreats.isNotEmpty()) {
            if (creep.getRangeTo(homeFlag) > 0) {
                creep.moveTo(homeFlag)
                return
            }

            if (creep.energyCarried() > 0 && homeTower != null &&
                (homeTower.store.getFreeCapacity(ENERGY_KEY) ?: 0) > 0 &&
                creep.getRangeTo(homeTower) <= 1
            ) {
                creep.transfer(homeTower, RESOURCE_ENERGY)
            }
            return
        }

        if (creep.energyCarried() > 0 && homeTower != null && (homeTower.store.getFreeCapacity(ENERGY_KEY) ?: 0) > 0) {
            if (creep.getRangeTo(homeTower) <= 1) {
                creep.transfer(homeTower, RESOURCE_ENERGY)
            } else {
                creep.moveTo(homeTower)
            }
            return
        }

        if (creep.energyCarried() == 0) {
            val energySupplier = selectEnergySupplier(
                creep = creep,
                context = context,
                preferredContainer = homeContainer,
                preferredTarget = homeTower ?: homeFlag,
            )

            if (energySupplier != null) {
                takeEnergy(creep, energySupplier)
                return
            }
        }

        when {
            homeThreats.any { threat -> homeFlag != null && threat.getRangeTo(homeFlag) <= HOME_CAPTURE_RANGE } && homeFlag != null -> {
                if (creep.getRangeTo(homeFlag) > 0) creep.moveTo(homeFlag)
            }

            homeTower != null && creep.getRangeTo(homeTower) > 1 -> creep.moveTo(homeTower)
            homeFlag != null && creep.getRangeTo(homeFlag) > 1 -> creep.moveTo(homeFlag)
        }
    }

    private fun workerStep(
        creep: Creep,
        context: Context,
        objectiveTarget: HasPosition?,
        guardTarget: HasPosition?,
    ) {
        if (nearestEnemyDistance(creep, context.enemyCreeps) <= 2 && guardTarget != null) {
            creep.moveTo(guardTarget)
            return
        }

        val towerTarget = selectTowerToCharge(context, objectiveTarget)
        if (creep.energyCarried() > 0 && towerTarget != null) {
            if (creep.getRangeTo(towerTarget) <= 1) {
                creep.transfer(towerTarget, RESOURCE_ENERGY)
            } else {
                creep.moveTo(towerTarget)
            }
            return
        }

        if (creep.energyCarried() == 0) {
            val energySupplier = selectEnergySupplier(
                creep = creep,
                context = context,
                preferredTarget = towerTarget ?: objectiveTarget,
            )

            if (energySupplier != null) {
                takeEnergy(creep, energySupplier)
                return
            }
        }

        val fallback = towerTarget ?: guardTarget ?: objectiveTarget
        if (fallback != null) {
            creep.moveTo(fallback)
        }
    }

    private fun healerStep(
        creep: Creep,
        context: Context,
        objectiveTarget: HasPosition?,
        guardTarget: HasPosition?,
    ) {
        val adjacentWounded = selectWoundedAlly(creep, context.myCreeps, maxRange = 1)
        if (adjacentWounded != null) {
            creep.heal(adjacentWounded)
        } else {
            val rangedWounded = selectWoundedAlly(creep, context.myCreeps, maxRange = 3)
            if (rangedWounded != null) {
                creep.rangedHeal(rangedWounded)
            }
        }

        val supportTarget = selectSupportTarget(context, objectiveTarget, guardTarget)
        if (supportTarget != null && creep.getRangeTo(supportTarget) > 1) {
            creep.moveTo(supportTarget)
        }
    }

    private fun rangerStep(
        creep: Creep,
        context: Context,
        objectiveTarget: HasPosition?,
        guardTarget: HasPosition?,
    ) {
        val inRangeTargets = context.enemyCreeps
            .filter { target -> creep.getRangeTo(target) <= 3 }
            .sortedWith(compareBy<Creep>({ it.hits }, { targetRolePriority(it) }, { creep.getRangeTo(it) }))

        when {
            inRangeTargets.size >= 3 -> creep.rangedMassAttack()
            inRangeTargets.isNotEmpty() -> creep.rangedAttack(inRangeTargets.first())
        }

        val fallback = guardTarget ?: selectSupportTarget(context, objectiveTarget, guardTarget)
        val bodyPartTarget = selectBodyPartTarget(creep, context)
        val chaseTarget = selectFocusTarget(creep, context.enemyCreeps, maxRange = 6)

        when {
            context.enemyCreeps.any { enemy -> creep.getRangeTo(enemy) <= 1 } && fallback != null -> creep.moveTo(fallback)
            inRangeTargets.isEmpty() && bodyPartTarget != null -> creep.moveTo(bodyPartTarget)
            inRangeTargets.isEmpty() && chaseTarget != null -> creep.moveTo(chaseTarget)
            inRangeTargets.isEmpty() && objectiveTarget != null -> creep.moveTo(objectiveTarget)
        }
    }

    private fun meleeStep(
        creep: Creep,
        context: Context,
        objectiveTarget: HasPosition?,
        guardTarget: HasPosition?,
    ) {
        val adjacentEnemy = selectFocusTarget(creep, context.enemyCreeps, maxRange = 1)
        if (adjacentEnemy != null) {
            creep.attack(adjacentEnemy)
            return
        }

        if (creep.hits * 2 < creep.hitsMax && guardTarget != null) {
            creep.moveTo(guardTarget)
            return
        }

        val nearbyBodyPart = selectBodyPartTarget(creep, context)
        val chaseTarget = selectFocusTarget(creep, context.enemyCreeps, maxRange = 4)
        val movementTarget = nearbyBodyPart ?: chaseTarget ?: objectiveTarget ?: guardTarget

        if (movementTarget != null) {
            creep.moveTo(movementTarget)
        }
    }

    private fun towerStep(
        tower: StructureTower,
        context: Context,
        homeFlag: Flag?,
    ) {
        val enemyTarget = selectTowerAttackTarget(tower, context, homeFlag)
        if (enemyTarget != null) {
            tower.attack(enemyTarget)
            return
        }

        val healTarget = context.myCreeps
            .filter { ally -> ally.hits < ally.hitsMax && tower.getRangeTo(ally) <= TOWER_ACTION_RANGE }
            .minByOrNull { ally -> ally.hits + defensePriority(roleOf(ally)) * 50 }

        if (healTarget != null) {
            tower.heal(healTarget)
        }
    }

    private fun selectTowerAttackTarget(
        tower: StructureTower,
        context: Context,
        homeFlag: Flag?,
    ): Creep? {
        return context.enemyCreeps
            .filter { enemy -> tower.getRangeTo(enemy) <= TOWER_ACTION_RANGE }
            .minWithOrNull(
                compareBy<Creep>(
                    { enemy -> towerTargetPriority(enemy, context, homeFlag) },
                    { enemy -> enemy.hits },
                    { enemy -> tower.getRangeTo(enemy) },
                )
            )
    }

    private fun towerTargetPriority(
        enemy: Creep,
        context: Context,
        homeFlag: Flag?,
    ): Int = when {
        homeFlag != null && enemy.getRangeTo(homeFlag) <= HOME_CAPTURE_RANGE -> 0
        context.myFlags.any { flag -> enemy.getRangeTo(flag) <= 2 } -> 1
        targetRolePriority(enemy) == 0 -> 2
        context.myCreeps.any { ally -> ally.getRangeTo(enemy) <= 3 } -> 3
        targetRolePriority(enemy) == 1 -> 4
        else -> 5
    }

    private fun selectObjectiveTarget(
        context: Context,
        homeFlag: Flag?,
        enemyHomeFlag: Flag?,
        homeThreats: List<Creep>,
    ): HasPosition? {
        val combatCreeps = context.myCreeps.filter { roleOf(it) != Role.WORKER }
        val anchorGroup = combatCreeps.ifEmpty { context.myCreeps }

        context.neutralFlags.minByOrNull { flag -> distanceFromGroup(anchorGroup, flag) }?.let { return it }

        if (shouldPushEnemyBase(context, homeFlag, enemyHomeFlag, homeThreats)) {
            val enemyHomeOwned = enemyHomeFlag?.takeIf { flag ->
                context.enemyFlags.any { it.id.toString() == flag.id.toString() }
            }
            return enemyHomeOwned ?: context.enemyFlags.minByOrNull { flag -> distanceFromGroup(anchorGroup, flag) }
        }

        if (context.myFlags.size <= context.enemyFlags.size) {
            return context.enemyFlags.minByOrNull { flag -> distanceFromGroup(anchorGroup, flag) }
        }

        return selectGuardTarget(context, homeFlag, homeThreats)
    }

    private fun shouldPushEnemyBase(
        context: Context,
        homeFlag: Flag?,
        enemyHomeFlag: Flag?,
        homeThreats: List<Creep>,
    ): Boolean {
        if (context.enemyFlags.isEmpty() || homeThreats.isNotEmpty()) {
            return false
        }

        val myCombat = context.myCreeps.count { roleOf(it) != Role.WORKER }
        val enemyCombat = context.enemyCreeps.count()
        val myHealthyCombat = context.myCreeps.count { roleOf(it) != Role.WORKER && it.hits * 10 >= it.hitsMax * 6 }
        val enemyHealthyCombat = context.enemyCreeps.count { it.hits * 10 >= it.hitsMax * 6 }
        val enemyHomeDefenders = enemyHomeFlag?.let { flag ->
            context.enemyCreeps.count { enemy -> enemy.getRangeTo(flag) <= ENEMY_HOME_DEFENSE_RANGE }
        } ?: enemyCombat
        val flagLead = context.myFlags.size > context.enemyFlags.size
        val homeSecure = homeFlag == null || context.enemyCreeps.none { enemy -> enemy.getRangeTo(homeFlag) <= HOME_THREAT_RANGE }
        val enemyBroken = enemyCombat <= maxOf(2, myCombat / 2) || enemyHealthyCombat <= maxOf(1, myHealthyCombat / 2)
        val enemyHomeOpen = enemyHomeDefenders <= 2

        return homeSecure && (
            (flagLead && enemyBroken) ||
                (flagLead && enemyHomeOpen && myHealthyCombat >= enemyHealthyCombat) ||
                (context.myFlags.size >= 3 && enemyHomeOpen && myCombat >= enemyCombat + 1)
            )
    }

    private fun selectGuardTarget(
        context: Context,
        homeFlag: Flag?,
        homeThreats: List<Creep>,
    ): HasPosition? {
        if (homeThreats.isNotEmpty()) {
            return homeFlag ?: homeThreats.first()
        }

        return context.myFlags.minByOrNull { flag ->
            nearestEnemyDistance(flag, context.enemyCreeps)
        } ?: homeFlag
    }

    private fun selectHomeThreats(context: Context, homeFlag: Flag?): List<Creep> {
        if (homeFlag == null) {
            return emptyList()
        }

        return context.enemyCreeps
            .filter { enemy -> enemy.getRangeTo(homeFlag) <= HOME_THREAT_RANGE }
            .sortedBy { enemy -> enemy.getRangeTo(homeFlag) }
    }

    private fun selectIncomingHomeThreats(
        context: Context,
        homeFlag: Flag?,
    ): List<Creep> {
        if (homeFlag == null) {
            return emptyList()
        }

        val previousEnemiesById = memory.beforeContext.enemyCreeps.associateBy { it.id.toString() }

        return context.enemyCreeps
            .filter { enemy ->
                val previous = previousEnemiesById[enemy.id.toString()] ?: return@filter false
                val currentRange = enemy.getRangeTo(homeFlag)
                val previousRange = previous.getRangeTo(homeFlag)

                currentRange in (HOME_THREAT_RANGE + 1)..EARLY_HOME_THREAT_RANGE &&
                    previousRange - currentRange >= HOME_APPROACH_DELTA
            }
            .sortedBy { enemy -> enemy.getRangeTo(homeFlag) }
    }

    private fun selectHomeDefenderIds(
        context: Context,
        homeFlag: Flag?,
        directHomeThreats: List<Creep>,
        incomingHomeThreats: List<Creep>,
    ): Set<String> {
        if (homeFlag == null || (directHomeThreats.isEmpty() && incomingHomeThreats.isEmpty())) {
            return emptySet()
        }

        val combatCreeps = context.myCreeps.filter { roleOf(it) != Role.WORKER }
        val desiredDefenders = when {
            directHomeThreats.size >= 4 -> 5
            directHomeThreats.size == 3 -> 4
            directHomeThreats.size == 2 -> 3
            directHomeThreats.size == 1 -> 2
            incomingHomeThreats.size >= 3 -> 3
            else -> 2
        }.coerceAtMost(combatCreeps.size)

        val healers = combatCreeps
            .filter { roleOf(it) == Role.HEALER }
            .sortedBy { it.getRangeTo(homeFlag) }

        val frontline = combatCreeps
            .filter { roleOf(it) != Role.HEALER }
            .sortedWith(
                compareBy<Creep>(
                    { creep -> creep.getRangeTo(homeFlag) },
                    { creep -> defensePriority(roleOf(creep)) },
                    { creep -> creep.hits },
                )
            )

        val selected = mutableListOf<Creep>()
        if (desiredDefenders >= 3) {
            healers.firstOrNull()?.let(selected::add)
        }
        selected += frontline.take(desiredDefenders - selected.size)

        if (selected.size < desiredDefenders) {
            selected += healers
                .filterNot { healer -> selected.any { it.id.toString() == healer.id.toString() } }
                .take(desiredDefenders - selected.size)
        }

        return selected.map { creep -> creep.id.toString() }.toSet()
    }

    private fun selectHomeWorkerId(
        context: Context,
        homeFlag: Flag?,
        homeTower: StructureTower?,
    ): String? {
        return context.myCreeps
            .filter { roleOf(it) == Role.WORKER }
            .minByOrNull { worker ->
                minOf(
                    homeFlag?.let { worker.getRangeTo(it) } ?: Int.MAX_VALUE,
                    homeTower?.let { worker.getRangeTo(it) } ?: Int.MAX_VALUE,
                )
            }
            ?.id
            ?.toString()
    }

    private fun selectHomeTower(
        context: Context,
        homeFlag: Flag?,
    ): StructureTower? {
        return context.towers.minByOrNull { tower ->
            homeFlag?.let { tower.getRangeTo(it) } ?: Int.MAX_VALUE
        }
    }

    private fun selectHomeContainer(
        context: Context,
        homeFlag: Flag?,
        homeTower: StructureTower?,
    ): StructureContainer? {
        return context.containers.minWithOrNull(
            compareBy<StructureContainer>(
                { container -> homeTower?.let { container.getRangeTo(it) } ?: Int.MAX_VALUE },
                { container -> homeFlag?.let { container.getRangeTo(it) } ?: Int.MAX_VALUE },
            )
        )
    }

    private fun selectTowerToCharge(
        context: Context,
        objectiveTarget: HasPosition?,
    ): StructureTower? {
        return context.myTowers
            .filter { tower -> (tower.store.getFreeCapacity(ENERGY_KEY) ?: 0) > 0 }
            .minWithOrNull(
                compareBy<StructureTower>(
                    { tower -> tower.energyStored() },
                    { tower -> objectiveTarget?.let { tower.getRangeTo(it) } ?: Int.MAX_VALUE },
                )
            )
    }

    private fun selectEnergySupplier(
        creep: Creep,
        context: Context,
        preferredContainer: StructureContainer? = null,
        preferredTarget: HasPosition? = null,
    ): HasPosition? {
        preferredContainer
            ?.takeIf { it.energyStored() > 0 }
            ?.let { return it }

        context.myContainers
            .filter { it.energyStored() > 0 }
            .minWithOrNull(
                compareBy<StructureContainer>(
                    { container -> preferredTarget?.let { container.getRangeTo(it) } ?: Int.MAX_VALUE },
                    { container -> creep.getRangeTo(container) },
                    { container -> -container.energyStored() },
                )
            )
            ?.let { return it }

        context.containers
            .filter { it.energyStored() > 0 }
            .minWithOrNull(
                compareBy<StructureContainer>(
                    { container -> preferredTarget?.let { container.getRangeTo(it) } ?: Int.MAX_VALUE },
                    { container -> creep.getRangeTo(container) },
                    { container -> -container.energyStored() },
                )
            )
            ?.let { return it }

        return selectHarvestSource(creep, context, preferredTarget)
    }

    private fun selectHarvestSource(
        creep: Creep,
        context: Context,
        target: HasPosition?,
    ): Source? {
        return context.sources
            .filter { source -> source.energy > 0 }
            .minByOrNull { source ->
                creep.getRangeTo(source) + (target?.let { source.getRangeTo(it) } ?: 0)
            }
    }

    private fun takeEnergy(
        creep: Creep,
        source: HasPosition,
    ) {
        when (source) {
            is StructureContainer -> {
                if (creep.getRangeTo(source) <= 1) {
                    creep.withdraw(source, RESOURCE_ENERGY)
                } else {
                    creep.moveTo(source)
                }
            }

            is Source -> {
                if (creep.getRangeTo(source) <= 1) {
                    creep.harvest(source)
                } else {
                    creep.moveTo(source)
                }
            }
        }
    }

    private fun selectWoundedAlly(
        creep: Creep,
        allies: List<Creep>,
        maxRange: Int,
    ): Creep? {
        return allies
            .filter { ally -> ally.hits < ally.hitsMax && creep.getRangeTo(ally) <= maxRange }
            .minByOrNull { ally -> ally.hits }
    }

    private fun selectSupportTarget(
        context: Context,
        objectiveTarget: HasPosition?,
        guardTarget: HasPosition?,
    ): HasPosition? {
        context.myCreeps
            .filter { it.hits < it.hitsMax }
            .minByOrNull { it.hits }
            ?.let { return it }

        context.myCreeps
            .filter { roleOf(it) != Role.WORKER }
            .minWithOrNull(
                compareBy<Creep>(
                    { creep -> objectiveTarget?.let { creep.getRangeTo(it) } ?: Int.MAX_VALUE },
                    { creep -> roleSupportPriority(roleOf(creep)) },
                )
            )
            ?.let { return it }

        return objectiveTarget ?: guardTarget
    }

    private fun selectBodyPartTarget(
        creep: Creep,
        context: Context,
    ): BodyPart? {
        if (memory.currentPhase == Phase.IN_BATTLE && nearestEnemyDistance(creep, context.enemyCreeps) <= 3) {
            return null
        }

        return context.bodyPartItems
            .filter { item -> creep.getRangeTo(item) <= 4 }
            .filter { item -> isBodyPartSafe(item, context) }
            .minWithOrNull(
                compareBy<BodyPart>(
                    { item -> bodyPartPriority(creep, item) },
                    { item -> creep.getRangeTo(item) },
                )
            )
    }

    private fun isBodyPartSafe(
        item: BodyPart,
        context: Context,
    ): Boolean {
        val allyDistance = distanceFromGroup(context.myCreeps, item)
        val enemyDistance = distanceFromGroup(context.enemyCreeps, item)
        return enemyDistance >= 4 || allyDistance <= enemyDistance
    }

    private fun bodyPartPriority(
        creep: Creep,
        item: BodyPart,
    ): Int {
        val role = roleOf(creep)
        return when {
            item.type == MOVE -> 0
            role == Role.HEALER && item.type == HEAL -> 1
            role == Role.RANGER && item.type == RANGED_ATTACK -> 1
            role == Role.MELEE && item.type == ATTACK -> 1
            item.type == HEAL -> 2
            item.type == RANGED_ATTACK -> 3
            item.type == ATTACK -> 4
            item.type == CARRY -> 5
            else -> 6
        }
    }

    private fun selectFocusTarget(
        creep: Creep,
        targets: List<Creep>,
        maxRange: Int,
    ): Creep? {
        return targets
            .filter { target -> creep.getRangeTo(target) <= maxRange }
            .minWithOrNull(
                compareBy<Creep>(
                    { target -> target.hits },
                    { target -> targetRolePriority(target) },
                    { target -> creep.getRangeTo(target) },
                )
            )
    }

    private fun targetRolePriority(creep: Creep): Int = when (creep.determineRole()) {
        Role.HEALER -> 0
        Role.WORKER -> 1
        Role.RANGER -> 2
        Role.MELEE -> 3
        Role.CREEP -> 4
    }

    private fun defensePriority(role: Role): Int = when (role) {
        Role.MELEE -> 0
        Role.RANGER -> 1
        Role.HEALER -> 2
        Role.CREEP -> 3
        Role.WORKER -> 4
    }

    private fun roleSupportPriority(role: Role): Int = when (role) {
        Role.MELEE -> 0
        Role.RANGER -> 1
        Role.HEALER -> 2
        Role.CREEP -> 3
        Role.WORKER -> 4
    }

    private fun distanceFromGroup(
        group: List<Creep>,
        target: HasPosition,
    ): Int {
        return group.minOfOrNull { creep -> creep.getRangeTo(target) } ?: Int.MAX_VALUE
    }

    private fun nearestEnemyDistance(
        position: HasPosition,
        enemies: List<Creep>,
    ): Int {
        return enemies.minOfOrNull { enemy -> enemy.getRangeTo(position) } ?: Int.MAX_VALUE
    }

    private fun resolveFlag(context: Context, flagId: String?): Flag? =
        context.flags.firstOrNull { it.id.toString() == flagId }

    private fun resolveTower(context: Context, towerId: String?): StructureTower? =
        context.towers.firstOrNull { it.id.toString() == towerId }

    private fun resolveContainer(context: Context, containerId: String?): StructureContainer? =
        context.containers.firstOrNull { it.id.toString() == containerId }

    private fun roleOf(creep: Creep): Role = memory.creepMemory[creep]?.role ?: creep.determineRole()

    private fun <T : GameObject> orderFor(
        performer: T,
        action: T.() -> Unit,
    ): Order<T> = Order(performer, bot.arena.mode.capturetheflag.model.Instructions { instruction(action) })

    private fun Creep.energyCarried(): Int = store.getUsedCapacity(ENERGY_KEY) ?: 0

    private fun StructureTower.energyStored(): Int = store.getUsedCapacity(ENERGY_KEY) ?: 0

    private fun StructureContainer.energyStored(): Int = store.getUsedCapacity(ENERGY_KEY) ?: 0

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

    companion object {
        private val ENERGY_KEY = RESOURCE_ENERGY.unsafeCast<String?>()
        private const val HOME_THREAT_RANGE = 12
        private const val EARLY_HOME_THREAT_RANGE = 18
        private const val HOME_APPROACH_DELTA = 1
        private const val HOME_CAPTURE_RANGE = 2
        private const val ENEMY_HOME_DEFENSE_RANGE = 6
        private const val TOWER_ACTION_RANGE = 20
    }
}
