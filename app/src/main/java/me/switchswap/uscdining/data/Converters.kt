package me.switchswap.uscdining.data

import androidx.room.TypeConverter
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType

class Converters {
    @TypeConverter
    fun diningHallTypeToId(diningHallType: DiningHallType): Int = diningHallType.id

    @TypeConverter
    fun itemTypeToType(itemType: ItemType): String = itemType.name
}