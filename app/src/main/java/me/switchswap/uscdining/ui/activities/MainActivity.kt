package me.switchswap.uscdining.ui.activities

import android.os.Bundle
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.app_bar_main.*
import me.switchswap.uscdining.R
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.parser.MenuManager
import me.switchswap.uscdining.ui.adapters.MenuPagerAdapter
import me.switchswap.uscdining.util.DateUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val dateUtil = DateUtil()
    private var isRefreshing = false

    private val viewPager by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<ViewPager>(R.id.viewpager)
    }

    private val tabs by lazy(LazyThreadSafetyMode.NONE) {
        findViewById<TabLayout>(R.id.tablayout)
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

        // Set action bar date
        setActionBarDate()

        // Set system prefs date
        dateUtil.writeDate(this, dateUtil.convertDate(Calendar.getInstance()))

        // Initialize views and setup view pager
        setupViewPager()

        // Set refresh layout listener
        val refreshLayout: SwipeRefreshLayout = findViewById(R.id.refresh_layout)
        refreshLayout.apply {
            setColorSchemeResources(R.color.colorAccent)

            setOnRefreshListener {
                // On refresh, use date from prefs
                reloadMenu(dateUtil.readDate(this@MainActivity))
            }
        }

        // Set button listener
        fab.setOnClickListener {
            MaterialDialog(this).show {
                val currentDate = Calendar.getInstance().apply {
                    val date = dateUtil.readDate(this@MainActivity)
                    time = Date(dateUtil.readDate(this@MainActivity))
                }
                datePicker(currentDate = currentDate) { _, date ->
                    // Use date (Calendar)
                    setActionBarDate(date)

                    // Update shared preference
                    val unixTimeStamp = dateUtil.convertDate(date)
                    dateUtil.writeDate(this@MainActivity, unixTimeStamp)

                    // Reload menu
                    reloadMenu(unixTimeStamp)
                }
            }
        }
        // Populate database from website if needed
        reloadMenu(dateUtil.readDate(this))
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
     * Setup [ViewPager] such that if it is scrolled horizontally, the [SwipeRefreshLayout] is not
     * activated by accident
     */
    private fun setupViewPager(){
        viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            // If scroll state is not idle, disable SwipeRefreshLayout
            override fun onPageScrollStateChanged(state: Int) {
                enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE)
            }
        })

        changeViewPager(R.id.nav_evk)
    }

    /**
     * @param selectedItem is the item selected from the navigation drawer
     * @see onOptionsItemSelected
     */
    private fun changeViewPager(@IdRes selectedItem : Int) {
        val fragmentPagerAdapter : FragmentStatePagerAdapter? = when(selectedItem) {
            R.id.nav_evk -> {
                title = "EVK"
                MenuPagerAdapter(supportFragmentManager, DiningHallType.EVK)
            }
            R.id.nav_parkside -> {
                title = "Parkside"
                MenuPagerAdapter(supportFragmentManager, DiningHallType.PARKSIDE)
            }
            R.id.nav_village -> {
                title = "Village"
                MenuPagerAdapter(supportFragmentManager, DiningHallType.VILLAGE)
            }
            else -> null
        }

        fragmentPagerAdapter?.also { adapter ->
            viewPager.adapter = adapter
            tabs.setupWithViewPager(viewPager)
        }
    }

    private fun setActionBarDate(calendar: Calendar = Calendar.getInstance()) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val toolbarDate : TextView? = findViewById(R.id.toolbar_date)
        toolbarDate?.text = getString(R.string.date_string, month + 1, day, year % 100)
    }

    private fun reloadFragments(fragments: List<Fragment>) {
        if(fragments.isEmpty()) return

        fragments.forEach {
            if (it.isVisible) {
                val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.detach(it)
                fragmentTransaction.attach(it)
                fragmentTransaction.commit()
            }
        }
    }

    /**
     * Reloads menu by checking database for items
     * If no items are found for a given day, populate database from website and load from there
     *
     * @param unixTimeStamp is the date for which to retrieve the menu
     */
    private fun reloadMenu(unixTimeStamp: Long?) {
        if(unixTimeStamp == null) return

        isRefreshing = true

        val refreshLayout: SwipeRefreshLayout = findViewById(R.id.refresh_layout)
        refreshLayout.isRefreshing = true


        doAsync{
            val menuManager = MenuManager(this@MainActivity)

            // Check if data exists already
            var menuExists: Boolean = menuManager.checkMenuExists(null, dateUtil.readDate(this@MainActivity))

            if(!menuExists){
                // Populate database from website
                Log.d("action_refresh", "Attempting to populate database...")
                menuExists = menuManager.populateDatabaseFromWebsite(Date(unixTimeStamp))
            }

            val hallStatus: Int = menuManager.getOpenDiningHalls(unixTimeStamp)
            enableDisableDiningHalls(hallStatus)

            runOnUiThread{
                if(menuExists){
                    Log.d("action_refresh", "Database populated.")
                    reloadFragments(supportFragmentManager.fragments)
                    toast("Loaded!")
                }
                else{
                    Log.d("action_refresh", "Something went wrong.")
                    longToast("Something went wrong!")
                }
                refreshLayout.isRefreshing = false

                isRefreshing = false
            }
        }
    }

    /**
     * Enables or disables [SwipeRefreshLayout]
     */
    private fun enableDisableSwipeRefresh(enable: Boolean) {
        if(isRefreshing) return

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.refresh_layout)
        if(swipeRefreshLayout != null) {
            swipeRefreshLayout.isEnabled = enable
        }
    }

    private fun enableDisableDiningHalls(hallStatus: Int) {
        when(hallStatus){
            111 -> return
            110 -> {
                nav_view.menu.findItem(R.id.nav_village)?.isEnabled = false
            }
            100 -> {
                nav_view.menu.findItem(R.id.nav_parkside)?.isEnabled = false
                nav_view.menu.findItem(R.id.nav_village)?.isEnabled = false
            }
            0 -> {
                nav_view.menu.findItem(R.id.nav_evk)?.isEnabled = false
                nav_view.menu.findItem(R.id.nav_parkside)?.isEnabled = false
                nav_view.menu.findItem(R.id.nav_village)?.isEnabled = false
            }
        }
    }

    private fun disableTab(tabLayout: TabLayout, index: Int) {
        (tabLayout.getChildAt(0) as ViewGroup).getChildAt(index).isEnabled = false
    }
    // todo: Make empty "times" in the day greyed out
    // todo: Handle brunch. Make Lunch grey w/ no items and Breakfast gold with brunch items

}