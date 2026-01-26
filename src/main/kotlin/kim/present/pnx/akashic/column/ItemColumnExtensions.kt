package kim.present.pnx.akashic.column

import cn.nukkit.item.Item
import cn.nukkit.nbt.SNBTParser
import cn.nukkit.nbt.tag.ByteArrayTag
import cn.nukkit.nbt.tag.ByteTag
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.nbt.tag.DoubleTag
import cn.nukkit.nbt.tag.FloatTag
import cn.nukkit.nbt.tag.IntArrayTag
import cn.nukkit.nbt.tag.IntTag
import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.LongTag
import cn.nukkit.nbt.tag.ShortTag
import cn.nukkit.nbt.tag.StringTag
import cn.nukkit.nbt.tag.Tag
import com.google.gson.GsonBuilder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

/**
 * Stores Item objects as SNBT strings.
 *
 * @param name The name of the column.
 * @return The created column.
 */
fun Table.item(name: String): Column<Item> {
    return registerColumn(name, ItemColumnType())
}

class ItemColumnType : ColumnType<Item>() {
    override fun sqlType(): String = "TEXT"

    override fun valueFromDB(value: Any): Item? {
        if (value is String) {
            val rootTag = SNBTParser.parse(value)

            val id = if (rootTag.containsString("id")) rootTag.getString("id") else "minecraft:air"
            val damage = if (rootTag.containsShort("Damage")) rootTag.getShort("Damage").toInt() else 0
            val count = if (rootTag.containsByte("Count")) rootTag.getByte("Count").toInt() else 1

            val item = Item.get(id, damage, count)
            if (rootTag.containsCompound("tag")) item.namedTag = rootTag.getCompound("tag")

            return item
        }
        return null
    }

    override fun notNullValueToDB(value: Item): Any {
        if (value.isNull) {
            return "{}"
        }

        return CompoundTag().apply {
            putString("id", value.id)

            if (value.count != 1) putByte("Count", value.count)
            if (value.meta != 0) putShort("Damage", value.meta)
            if (value.hasCompoundTag()) putCompound("tag", value.namedTag)
        }.toSnbt()
    }

    /**
     * Extension function to convert PNX Tag objects to standard SNBT format strings.
     * This function handles the conversion without external libraries.
     */
    private fun Tag.toSnbt(): String {
        return when (this) {
            is CompoundTag -> {
                val sb = StringBuilder("{")
                this.tags.entries.forEachIndexed { index, (key, tag) ->
                    if (index > 0) sb.append(",")

                    val quotedKey = if (key.matches(Regex("^[a-zA-Z0-9_]+$"))) key else "\"$key\""
                    sb.append(quotedKey).append(":").append(tag.toSnbt())
                }
                sb.append("}")
                sb.toString()
            }

            is ListTag<*> -> {
                val sb = StringBuilder("[")
                this.all.forEachIndexed { index, tag ->
                    if (index > 0) sb.append(",")
                    sb.append(tag.toSnbt())
                }
                sb.append("]")
                sb.toString()
            }

            is StringTag -> GsonBuilder().create().toJson(this.data)
            is ByteTag -> "${this.data}b"
            is ShortTag -> "${this.data}s"
            is IntTag -> "${this.data}"
            is LongTag -> "${this.data}L"
            is FloatTag -> "${this.data}f"
            is DoubleTag -> "${this.data}d"
            is ByteArrayTag -> "[B;${this.data.joinToString(",") { "${it}b" }}]"
            is IntArrayTag -> "[I;${this.data.joinToString(",")}]"
            else -> this.toString()
        }
    }
}
