package kim.present.pnx.akashic.column

import cn.nukkit.Server
import cn.nukkit.level.Location
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

/**
 * Stores Location objects as strings in the format "world_name:x:y:z:yaw:pitch".
 *
 * @param name The name of the column.
 * @return The created column.
 */
fun Table.location(name: String): Column<Location> {
    return registerColumn(name, LocationColumnType())
}

class LocationColumnType : ColumnType<Location>() {
    override fun sqlType(): String = "TEXT"

    override fun valueFromDB(value: Any): Location? {
        if (value is String) {
            val parts = value.split(":")
            if (parts.size >= 4) {
                val level = Server.getInstance().getLevelByName(parts[0])
                return Location(
                    parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(),
                    parts.getOrNull(4)?.toDouble() ?: 0.0, // yaw
                    parts.getOrNull(5)?.toDouble() ?: 0.0, // pitch
                    level
                )
            }
        }
        return null
    }

    override fun notNullValueToDB(value: Location): Any {
        return "${value.level.name}:${value.x}:${value.y}:${value.z}:${value.yaw}:${value.pitch}"
    }
}
