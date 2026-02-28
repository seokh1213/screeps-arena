package bot.arena.mode.capturetheflag

import bot.arena.mode.Arena
import bot.arena.mode.capturetheflag.model.Order

class CaptureTheFlagArena : Arena {
    val overmind = Overmind()

    override fun loop() {
        val orders = overmind.commandOrder()
        performOrders(orders)
    }

    private fun performOrders(orders: List<Order<*>>) {
        println("Orders(${orders.size}) :" + orders.joinToString("\n"))

        orders.forEach { order ->
            order.perform()
        }
    }
}
