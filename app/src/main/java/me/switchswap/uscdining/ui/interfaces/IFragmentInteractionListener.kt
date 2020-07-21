package me.switchswap.uscdining.ui.interfaces

interface IFragmentInteractionListener {
    /**
     * Update Tab color to indicate brunch
     */
    fun makeTabBrunch()

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
     * Check if empty dining hall
     */
    // fun isEmptyHall(): Boolean
}