package kim.present.pnx.akashic.event

import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList

/**
 * Called when the Akashic database connection fails.
 *
 * @param exception The exception that caused the failure
 * @param attempt The current attempt number
 */
class AkashicConnectionFailedEvent(val exception: Exception, val attempt: Int) : Event() {
    companion object {
        @JvmField
        val handlerList = HandlerList()

        @JvmStatic
        fun getHandlers() = handlerList
    }
}
