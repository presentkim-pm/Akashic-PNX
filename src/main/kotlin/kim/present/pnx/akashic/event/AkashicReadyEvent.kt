package kim.present.pnx.akashic.event

import cn.nukkit.event.Event
import cn.nukkit.event.HandlerList
import org.jetbrains.exposed.sql.Database

/**
 * Called when the Akashic database is ready.
 *
 * @param database The database instance
 */
class AkashicReadyEvent(val database: Database) : Event() {
    companion object {
        @JvmField
        val handlerList = HandlerList()

        @JvmStatic
        fun getHandlers() = handlerList
    }
}
