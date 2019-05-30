package me.switchswap.uscdining.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import me.switchswap.uscdining.R
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.parser.MenuManager
import me.switchswap.uscdining.parser.database
import me.switchswap.uscdining.ui.adapters.MenuPagerAdapter
import me.switchswap.uscdining.ui.fragments.DatePickerFragment
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
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

        // Set button listener
        fab.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.show(supportFragmentManager, "datePicker")
        }
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Set navigation drawer listener
        nav_view.setNavigationItemSelectedListener(this)


        // Set action bar date
        setActionBarDate()

        // Initialize views and setup view pager
        setupViewPager(R.id.nav_evk)

        // Delete database values [DEBUG]
        database.use{
            delete("MenuItems")
            delete("ItemAllergens")
        }

        // Populate database from website if needed
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
                // Populate database from website
                doAsync{
                    val menuManager = MenuManager(this@MainActivity)
                    val databasePopulated = menuManager.populateDatabaseFromWebsite(this@MainActivity, Date(1548971445000))

                    runOnUiThread{
                        if(databasePopulated){
                            reloadFragments(supportFragmentManager.fragments)
                            toast("Success!")
                        }
                        else{
                            longToast("Something went wrong!")
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        setupViewPager(item.itemId)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * @param selectedItem is the item selected from the navigation drawer
     * @see onOptionsItemSelected
     */
    private fun setupViewPager(@IdRes selectedItem : Int) {
        val fragmentPagerAdapter : FragmentStatePagerAdapter? = when(selectedItem) {
            R.id.nav_evk -> {
                Log.i("setupViewPager", "EVK Selected")
                MenuPagerAdapter(supportFragmentManager, DiningHallType.EVK)
            }
            R.id.nav_parkside -> {
                Log.i("setupViewPager", "Parkside Selected")
                MenuPagerAdapter(supportFragmentManager, DiningHallType.PARKSIDE)
            }
            R.id.nav_village -> {
                Log.i("setupViewPager", "Village Selected")
                MenuPagerAdapter(supportFragmentManager, DiningHallType.VILLAGE)
            }
            else -> null
        }

        fragmentPagerAdapter?.also { adapter ->
            viewPager.adapter = adapter
            tabs.setupWithViewPager(viewPager)
        }
    }

    private fun setActionBarDate() {
        val calendar = Calendar.getInstance()
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
    // todo: fix date or use string throughout
    // todo: background load needed on first menu creation, date change
    // todo: make gui look pretty / make allergens have custom colored border
}