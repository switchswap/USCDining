package me.switchswap.uscdining.menu

import androidx.room.TypeConverter
import models.DiningHallType

class Converters {
    @TypeConverter
    fun diningHallTypeToId(diningHallType: DiningHallType): Int = diningHallType.id
}