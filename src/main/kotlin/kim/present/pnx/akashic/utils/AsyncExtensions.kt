package kim.present.pnx.akashic.utils


import kim.present.pnx.akashic.Akashic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CompletableFuture

// Separate IO thread pool for DB operations
val dbScope = CoroutineScope(Dispatchers.IO)

/**
 * [Kotlin Style]
 * Performs DB operations without blocking the main thread. (Fire and Forget)
 * Ex: Saving player logs, item backups, etc. when waiting for the result is not necessary
 *
 * ```kotlin
 * asyncTransaction {
 *     CropEntity.new { ... }
 * }
 * ```
 */
fun <T> asyncTransaction(block: Transaction.() -> T) {
    dbScope.launch {
        transaction(Akashic.db) {
            block()
        }
    }
}

/**
 * [Java/CompletableFuture Compatible Style]
 * Used when the result is needed after a DB operation.
 * Ex: Loading player data (Allow connection only after receiving the result)
 */
fun <T> futureTransaction(block: Transaction.() -> T): CompletableFuture<T> {
    val future = CompletableFuture<T>()
    dbScope.launch {
        try {
            val result = transaction(Akashic.db) { block() }
            future.complete(result)
        } catch (e: Exception) {
            future.completeExceptionally(e)
        }
    }
    return future
}
