package kim.present.pnx.akashic.column

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.lang.reflect.Type

private val gson = Gson()

/**
 * A column type that converts arbitrary objects to JSON strings and stores them in the DB.
 *
 * Example:
 * ```kotlin
 * val stats = json<StatData>("stats")
 * ```
 *
 * @param name The name of the column.
 * @return The created column.
 */
inline fun <reified T : Any> Table.json(name: String): Column<T> {
    return registerColumn(name, JsonColumnType<T>(object : TypeToken<T>() {}.type))
}

class JsonColumnType<T>(private val type: Type) : ColumnType<T>() {
    override fun sqlType(): String = "TEXT"

    override fun valueFromDB(value: Any): T? {
        return if (value is String) {
            gson.fromJson(value, type)
        } else {
            null
        }
    }

    override fun notNullValueToDB(value: T & Any): Any {
        return gson.toJson(value)
    }
}
