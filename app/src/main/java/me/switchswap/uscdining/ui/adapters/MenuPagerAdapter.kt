package me.switchswap.uscdining.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import me.switchswap.diningmenu.models.DiningHallType
import me.switchswap.diningmenu.models.ItemType
import me.switchswap.uscdining.ui.fragments.MenuFragment

class MenuPagerAdapter(fm: FragmentManager, private val diningHallType: DiningHallType)
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

    override fun getCount(): Int = 3

    override fun getItem(position: Int) : Fragment = when(position) {
        0 -> MenuFragment.newInstance(
                MenuFragment.Payload(
                        diningHallType = diningHallType,
                        itemType = ItemType.BREAKFAST
                )
        )
        1 -> MenuFragment.newInstance(
                MenuFragment.Payload(
                        diningHallType = diningHallType,
                        itemType = ItemType.LUNCH
                )
        )
        2 -> MenuFragment.newInstance(
                MenuFragment.Payload(
                        diningHallType = diningHallType,
                        itemType = ItemType.DINNER
                )
        )
        else -> Fragment()
    }

    override fun getPageTitle(position: Int): CharSequence? = when(position){
        0 -> "Breakfast"
        1 -> "Lunch"
        else -> "Dinner"
    }
}