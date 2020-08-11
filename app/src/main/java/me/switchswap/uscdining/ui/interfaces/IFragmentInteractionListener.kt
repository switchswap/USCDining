package me.switchswap.uscdining.ui.interfaces

import me.switchswap.diningmenu.models.DiningHallType

interface IFragmentInteractionListener {
    /**
     * Update tab color to indicate brunch if needed
     */
    fun configureBrunch(diningHallType: DiningHallType, date: Long)

    /**
     * Updates the navigation view depending on which dining halls are open
     */
    fun configureDiningHalls(date: Long)

    /**
     * Ge refreshing status
     */
    fun getRefreshing(): Boolean

    /**
     * Set refreshing status
     */
    fun setRefreshing(status: Boolean)

    /**
     * Disable navigation drawer
     */
    fun disableNavDrawer()
}