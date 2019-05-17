package me.switchswap.uscdining.ui.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.ui.fragments.MenuFragment
import org.jetbrains.anko.db.NULL

class MenuPagerAdapter(fm: FragmentManager, private val diningHallType: DiningHallType) : FragmentPagerAdapter(fm){ //private val menu: Menu
    override fun getCount(): Int {
        return 3
    }

    override fun getItem(position: Int): Fragment {
        Log.i("MenuPageAdapter", "Got Item $position")
        var fragment: Fragment? = null
        when(position){
            0 -> fragment = MenuFragment.newInstance(diningHallType, MealType.BREAKFAST)
            1 -> fragment = MenuFragment.newInstance(diningHallType, MealType.LUNCH)
            2 -> fragment =MenuFragment.newInstance(diningHallType, MealType.DINNER)
        }
        return fragment!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position){
            0 -> "Breakfast"
            1 -> "Lunch"
            2 -> "Dinner"
            else -> "ERROR"
        }
    }
}