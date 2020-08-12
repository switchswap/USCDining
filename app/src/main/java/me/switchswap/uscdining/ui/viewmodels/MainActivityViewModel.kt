package me.switchswap.uscdining.ui.viewmodels

import me.switchswap.diningmenu.models.DiningHallType

class MainActivityViewModel {
    var currentDiningHall: Int = -1

    private var isRefreshing: Boolean = false

    fun getRefreshing(): Boolean {
        return isRefreshing
    }

    fun setRefreshing(status: Boolean) {
        isRefreshing = status
    }
}