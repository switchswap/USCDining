package me.switchswap.uscdining.ui.activities

import android.content.Intent
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import me.switchswap.uscdining.R
import me.switchswap.uscdining.ui.adapters.MenuPagerAdapter
import me.switchswap.uscdining.ui.interfaces.FragmentInteractionListener
import me.switchswap.uscdining.util.DateUtil
import models.DiningHallType
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, FragmentInteractionListener {

    private val viewPager by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ViewPager>(R.id.viewpager)
    }

    private val tabLayout by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TabLayout>(R.id.tablayout)
    }

    private val dateUtil by lazy {
        DateUtil(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Set navigation drawer listener
        nav_view.setNavigationItemSelectedListener(this)
        viewPager.offscreenPageLimit = 2
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Set system prefs date
        dateUtil.writeDate(DateUtil.convertDate(Calendar.getInstance()))

        // Set action bar date
        setActionBarDate()

        // Initialize views and setup view pager
        setupViewPager()

        // Set button listener for date button
        fab_date.setOnClickListener {
            // Create date picker on click
            MaterialDialog(this).show {
                val currentDate = Calendar.getInstance().apply {
                    time = Date(dateUtil.readDate())
                }

                // Todo: Set maxDate parameter to one week past current date
                datePicker(currentDate = currentDate) { _, date ->
                    // Use date (Calendar)
                    setActionBarDate(date)

                    // Update shared preference
                    val unixTimeStamp = DateUtil.convertDate(date)
                    dateUtil.writeDate(unixTimeStamp)

                    // Reconfigure dinning halls upon date change
                    configureDiningHalls()
                }
            }
        }

        nav_settings.setOnClickListener {
            val settingsIntent = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }

        // Populate database from website if needed
        configureDiningHalls()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu
        // This adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when(item.itemId) {
            R.id.action_refresh -> {
                //reloadMenu(1548971445000)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        changeViewPager(item.itemId)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Setup [ViewPager] such that it shows the correct tab as set by the user in settings
     */
    private fun setupViewPager() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        // Todo: Think about inlining this variable even though I don't like it
        // Todo: Make values not hard-coded
        val defaultHall: String = sharedPreferences.getString(getString(R.string.pref_default_hall), "") ?: ""
        when (defaultHall) {
            "evk" -> changeViewPager(R.id.nav_evk)
            "parkside" -> changeViewPager(R.id.nav_parkside)
            "village" -> changeViewPager(R.id.nav_village)
            else -> changeViewPager(R.id.nav_evk)
        }
    }

    /**
     * @param selectedItem is the item selected from the navigation drawer
     * @see onOptionsItemSelected
     */
    private fun changeViewPager(@IdRes selectedItem : Int) {
        val fragmentPagerAdapter : FragmentStatePagerAdapter? = when(selectedItem) {
            R.id.nav_evk -> {
                title = "EVK"
                nav_view.setCheckedItem(R.id.nav_evk)
                MenuPagerAdapter(supportFragmentManager, DiningHallType.EVK)
            }
            R.id.nav_parkside -> {
                title = "Parkside"
                nav_view.setCheckedItem(R.id.nav_parkside)
                MenuPagerAdapter(supportFragmentManager, DiningHallType.PARKSIDE)
            }
            R.id.nav_village -> {
                title = "Village"
                nav_view.setCheckedItem(R.id.nav_village)
                MenuPagerAdapter(supportFragmentManager, DiningHallType.VILLAGE)
            }
            else -> null
        }

        fragmentPagerAdapter?.also { adapter ->
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)
        }
    }

    private fun setActionBarDate(calendar: Calendar = Calendar.getInstance()) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val toolbarDate : TextView? = findViewById(R.id.toolbar_date)
        toolbarDate?.text = getString(R.string.date_string, month + 1, day, year % 100)
    }

    /**
     * Updates the navigation view depending on the presence of certain items
     *
     * @param unixTimeStamp is the date for which to retrieve the menu
     */
    // Todo: Function may or may not be needed
    private fun configureDiningHalls(unixTimeStamp: Long? = dateUtil.readDate()) {
        if(unixTimeStamp == null) return
    }

    override fun makeTabBrunch() {
        val color: Int = ContextCompat.getColor(this, R.color.colorAccent)

        val breakfastTab: TextView? = tabLayout.getTabAt(0)?.view?.getChildAt(1) as TextView?
        breakfastTab?.setTextColor(color)

        val lunchTab: TextView? = tabLayout.getTabAt(1)?.view?.getChildAt(1) as TextView?
        lunchTab?.setTextColor(color)
    }

    // todo: Make empty "times" in the day greyed out
    companion object {
        val TAG = MainActivity::class.java.simpleName

    }
}