package me.switchswap.uscdining.ui.viewmodels

import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    var currentDiningHall: Int = -1

    private var isRefreshing: Boolean = false

    fun getRefreshing(): Boolean {
        return isRefreshing
    }

    fun setRefreshing(status: Boolean) {
        isRefreshing = status
    }
}