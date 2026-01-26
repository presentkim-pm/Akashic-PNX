package kim.present.pnx.akashic.utils

import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.plugin.Plugin
import kim.present.pnx.akashic.Akashic
import kim.present.pnx.akashic.event.AkashicConnectionFailedEvent
import kim.present.pnx.akashic.event.AkashicReadyEvent
import org.jetbrains.exposed.sql.Database

/**
 * Executes the block when the Akashic database is ready.
 * If already connected, it executes immediately; otherwise, it executes when the connection event occurs.
 *
 * @param plugin The plugin instance using this utility (for event registration)
 * @param block The logic to execute after database connection (provides Database instance)
 */
fun onAkashicReady(plugin: Plugin, block: (Database) -> Unit) {
    if (Akashic.isReady()) {
        // Execute immediately if already ready
        block(Akashic.db)
    } else {
        // Register a one-time listener if not ready
        plugin.server.pluginManager.registerEvents(object : Listener {
            @EventHandler
            fun onAkashicReady(event: AkashicReadyEvent) {
                block(event.database)
                // Unregister listener after execution (prevent memory leaks)
                AkashicReadyEvent.handlerList.unregister(this)
            }
        }, plugin)
    }
}

/**
 * Executes the block when the Akashic database connection fails.
 *
 * @param plugin The plugin instance using this utility (for event registration)
 * @param block The logic to execute on database connection failure (provides Exception and attempt count)
 */
fun onAkashicConnectionFailed(plugin: Plugin, block: (Exception, Int) -> Unit) {
    plugin.server.pluginManager.registerEvents(object : Listener {
        @EventHandler
        fun onAkashicConnectionFailed(event: AkashicConnectionFailedEvent) {
            block(event.exception, event.attempt)
        }
    }, plugin)
}

