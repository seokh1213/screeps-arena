package bot.arena.mode.capturetheflag

import bot.arena.mode.Arena
import screeps.bindings.arena.Creep
import screeps.bindings.arena.Flag
import screeps.bindings.arena.game.getObjectsByPrototype

class CaptureTheFlagArena : Arena {
    val overmind = Overmind()

    override fun loop() {
        val (myCreeps, enemyCreps) = getObjectsByPrototype(Creep).partition { it.my }
        val flags = getObjectsByPrototype(Flag)

        val orders = overmind.commandOrder()

//        myCreeps.forEach { myCreep ->
//            val nearFlag = flags.filter { it.my != true }
//                .minByOrNull { it.getRangeTo(myCreep) }
//                ?: run {
//                    println("[WARN] can't find any nearest flag.")
//                    return@forEach
//                }
//
//            myCreep.moveTo(nearFlag)
//        }
    }
}
