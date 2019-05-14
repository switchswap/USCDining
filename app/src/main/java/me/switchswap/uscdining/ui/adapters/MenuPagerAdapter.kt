package me.switchswap.uscdining.ui.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.switchswap.uscdining.models.Menu

class MenuPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){ //private val menu: Menu
    private val fragments : ArrayList<Fragment> = ArrayList()
    private val fragmentTitles : ArrayList<String> = ArrayList()

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        Log.i("MenuPageAdapter", "Got Item $position")
        return fragments[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        fragmentTitles.add(title)
    }
    override fun getPageTitle(position: Int): CharSequence? {
        Log.i("MenuPageAdapter", "Got Page Title $position")
        return fragmentTitles[position]
    }
}