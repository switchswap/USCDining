package me.switchswap.uscdining.data

import androidx.room.TypeConverter
import models.DiningHallType
import models.ItemType

class Converters {
    @TypeConverter
    fun diningHallTypeToId(diningHallType: DiningHallType): Int = diningHallType.id

    @TypeConverter
    fun itemTypeToType(itemType: ItemType): String = itemType.typeName
}