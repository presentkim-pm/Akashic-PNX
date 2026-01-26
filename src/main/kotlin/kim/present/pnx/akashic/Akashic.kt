package kim.present.pnx.akashic

import cn.nukkit.Server
import cn.nukkit.lang.PluginI18n
import cn.nukkit.lang.PluginI18nManager
import cn.nukkit.plugin.PluginBase
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kim.present.pnx.akashic.event.AkashicConnectionFailedEvent
import kim.present.pnx.akashic.event.AkashicReadyEvent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main class for the Akashic plugin.
 * This plugin handles database connections using HikariCP and Exposed.
 */
class Akashic : PluginBase() {
    companion object {
        lateinit var I18N: PluginI18n
        lateinit var db: Database
            private set

        private val _isReady = AtomicBoolean(false)

        /**
         * Checks if the database connection is established and ready.
         *
         * @return true if the database is ready, false otherwise.
         */
        fun isReady(): Boolean = _isReady.get()

        /**
         * Translates a message key to the server's default language.
         *
         * @param key The translation key.
         * @param args Arguments to replace in the translation string.
         * @return The translated string.
         */
        fun i18n(key: String, vararg args: Any?): String {
            return I18N.tr(Server.getInstance().languageCode, key, *args)
        }
    }

    private lateinit var dataSource: HikariDataSource

    override fun onLoad() {
        I18N = PluginI18nManager.register(this)
    }

    override fun onEnable() {
        saveDefaultConfig()

        logger.info(i18n("akashic.init"))
        this.server.scheduler.scheduleTask(this, {
            connectDatabaseAsync()
        }, true)
    }

    override fun onDisable() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            logger.info(i18n("akashic.db.closing"))
            dataSource.close()
        }
        _isReady.set(false)
    }

    /**
     * Connects to the database asynchronously.
     * It attempts to connect repeatedly until successful or the plugin is disabled.
     */
    private fun connectDatabaseAsync() {
        var attempt = 0

        // Repeat if the plugin is enabled and not yet connected
        while (this.isEnabled && !_isReady.get()) {
            attempt++
            try {
                val cfg = config.getSection("database")
                val poolCfg = config.getSection("pool")

                val host = cfg.getString("host", "localhost")
                val port = cfg.getInt("port", 3306)
                val schema = cfg.getString("schema", "akashic")
                val username = cfg.getString("username", "root")
                val password = cfg.getString("password", "")

                val hikariConfig = HikariConfig().apply {
                    jdbcUrl = "jdbc:mariadb://$host:$port/$schema"
                    this.username = username
                    this.password = password
                    driverClassName = "org.mariadb.jdbc.Driver"

                    maximumPoolSize = poolCfg.getInt("maximum-size", 10)
                    minimumIdle = poolCfg.getInt("minimum-idle", 5)
                    connectionTimeout = poolCfg.getLong("connection-timeout", 30000)

                    addDataSourceProperty("cachePrepStmts", "true")
                    addDataSourceProperty("prepStmtCacheSize", "250")
                    addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                    addDataSourceProperty("useServerPrepStmts", "true")
                    addDataSourceProperty("useLocalSessionState", "true")
                    addDataSourceProperty("rewriteBatchedStatements", "true")
                    addDataSourceProperty("cacheResultSetMetadata", "true")
                    addDataSourceProperty("maintainTimeStats", "false")
                }

                dataSource = HikariDataSource(hikariConfig)
                db = Database.connect(dataSource)
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_READ_COMMITTED

                // Handle successful connection
                _isReady.set(true)
                logger.info(i18n("akashic.db.connected"))

                // Call event on the main thread (notify other plugins)
                this.server.scheduler.scheduleTask(this) {
                    this.server.pluginManager.callEvent(AkashicReadyEvent(db))
                }
                return // End loop

            } catch (e: Exception) {
                logger.error("${i18n("akashic.db.error")} (Attempt: $attempt)")
                logger.error(e.message) // Print only the message to prevent log pollution, rather than the full stack trace

                // Call event on the main thread
                this.server.scheduler.scheduleTask(this) {
                    this.server.pluginManager.callEvent(AkashicConnectionFailedEvent(e, attempt))
                }

                // Wait for retry (5 seconds)
                try {
                    Thread.sleep(5000)
                } catch (_: InterruptedException) {
                    return
                }
            }
        }
    }
}
