package me.switchswap.uscdining.ui.activities

import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import me.switchswap.uscdining.R
import me.switchswap.uscdining.ui.adapters.MenuPagerAdapter
import me.switchswap.uscdining.ui.fragments.DatePickerFragment
import me.switchswap.uscdining.ui.fragments.MenuFragment
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Set date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val toolbarDate : TextView = findViewById<TextView>(R.id.toolbar_date)
        toolbarDate.text = getString(R.string.date_string, month + 1, day, year % 100)

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

        // Initialize views and setup view pager
        initViews()
        setupViewPager()

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
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_parkside -> {

            }
            R.id.nav_evk -> {

            }
            R.id.nav_village -> {

            }
//            R.id.nav_settings -> {
//
//            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun initViews() {
        tabs = findViewById(R.id.tablayout)
        viewPager = findViewById(R.id.viewpager)
    }

    private fun setupViewPager() {
        val adapter = MenuPagerAdapter(supportFragmentManager)

        val breakfastFragment: MenuFragment = MenuFragment()
        val lunchFragment: MenuFragment = MenuFragment()
        val dinnerFragment: MenuFragment = MenuFragment()

        adapter.addFragment(breakfastFragment, "Breakfast")
        adapter.addFragment(lunchFragment, "Lunch")
        adapter.addFragment(dinnerFragment, "Dinner")

        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }
}
