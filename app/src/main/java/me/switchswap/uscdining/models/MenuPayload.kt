package me.switchswap.uscdining.models

import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType

data class MenuPayload(val diningHallType: DiningHallType, val itemType: ItemType, val date: Long) {
}