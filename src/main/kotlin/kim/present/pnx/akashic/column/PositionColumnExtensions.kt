package kim.present.pnx.akashic.column

import cn.nukkit.Server
import cn.nukkit.level.Position
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

/**
 * Stores Position objects as strings in the format "world_name:x:y:z".
 *
 * @param name The name of the column.
 * @return The created column.
 */
fun Table.position(name: String): Column<Position> {
    return registerColumn(name, PositionColumnType())
}

class PositionColumnType : ColumnType<Position>() {
    override fun sqlType(): String = "TEXT"

    override fun valueFromDB(value: Any): Position? {
        if (value is String) {
            val parts = value.split(":")
            if (parts.size >= 4) {
                val level = Server.getInstance().getLevelByName(parts[0])
                return Position(
                    parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(),
                    level
                )
            }
        }
        return null
    }

    override fun notNullValueToDB(value: Position): Any {
        return "${value.level.name}:${value.x}:${value.y}:${value.z}"
    }
}
