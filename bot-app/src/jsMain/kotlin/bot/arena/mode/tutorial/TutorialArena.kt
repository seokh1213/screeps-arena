package bot.arena.mode.tutorial

import bot.arena.mode.Arena
import bot.arena.mode.tutorial.strategy.AttackerBehaviorStrategy
import bot.arena.mode.tutorial.strategy.AttackerSpawnStrategy
import bot.arena.mode.tutorial.strategy.NaiveWorkerBehaviorStrategy
import bot.arena.mode.tutorial.strategy.NaiveWorkerSpawnStrategy
import bot.arena.strategy.ParallelStrategy
import bot.arena.strategy.SequentialStrategy
import screeps.bindings.arena.StructureSpawn
import screeps.bindings.arena.game.PrototypeStructureSpawn
import screeps.bindings.arena.game.getObjectsByPrototype

class TutorialArena : Arena {

    private val mySpawn: StructureSpawn by lazy {
        getObjectsByPrototype(PrototypeStructureSpawn)
            .find { it.my == true }
            ?: throw IllegalStateException("Cannot find a my spawn of an arena!")
    }

    /**
     * 소환 파이프라인 (순서대로 실행, 각 전략이 isDone되면 다음으로):
     * 1. Worker 3마리
     * 2. Attacker 2마리
     * 3. Worker 2마리 추가
     */
    private val spawnStrategies = SequentialStrategy(
        NaiveWorkerSpawnStrategy(mySpawn = mySpawn, maxWorkers = 3),
        AttackerSpawnStrategy(mySpawn = mySpawn, maxAttackers = 2),
        NaiveWorkerSpawnStrategy(mySpawn = mySpawn, maxWorkers = 4),
        AttackerSpawnStrategy(mySpawn = mySpawn, maxAttackers = 5),
    )

    /**
     * 행동 전략 (모두 무한 실행, 매 틱 동시에 동작):
     * - Worker: source 수집 → spawn 전달
     * - Attacker: 적 creep/spawn 공격
     */
    private val behaviorStrategies = ParallelStrategy(
        NaiveWorkerBehaviorStrategy(mySpawn = mySpawn),
        AttackerBehaviorStrategy(mySpawn = mySpawn),
    )

    override fun loop() {
        spawnStrategies.tick()
        behaviorStrategies.tick()
    }
}
