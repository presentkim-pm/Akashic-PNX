package kim.present.pnx.akashic.column

import cn.nukkit.math.Vector3
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

/**
 * Stores Vector3 objects as strings in the format "x:y:z".
 *
 * @param name The name of the column.
 * @return The created column.
 */
fun Table.vector3(name: String): Column<Vector3> {
    return registerColumn(name, Vector3ColumnType())
}

class Vector3ColumnType : ColumnType<Vector3>() {
    override fun sqlType(): String = "TEXT"

    override fun valueFromDB(value: Any): Vector3? {
        if (value is String) {
            val parts = value.split(":")
            if (parts.size >= 3) {
                return Vector3(
                    parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble()
                )
            }
        }
        return null
    }

    override fun notNullValueToDB(value: Vector3): Any {
        return "${value.x}:${value.y}:${value.z}"
    }
}
