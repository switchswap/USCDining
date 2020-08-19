package me.switchswap.uscdining.extensions

import me.switchswap.diningmenu.models.DiningMenu

fun DiningMenu.isClosed(): Boolean {
    return parkside.isClosed() && evk.isClosed() && village.isClosed()
}