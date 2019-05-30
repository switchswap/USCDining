package me.switchswap.uscdining

import android.util.Log
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.models.MenuItem
import me.switchswap.uscdining.parser.MenuManager
import me.switchswap.uscdining.parser.database
import org.jetbrains.anko.db.delete
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.ArrayList

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class MenuManagerTest{
    private val appContext by lazy { InstrumentationRegistry.getInstrumentation().context }

    @Test
    fun populateDatabaseFromWebsiteTest(){
        Log.d("populateDatabaseFromWebsiteTest", "Starting...")
        val date = Date(1548971445000)

        Log.d("populateDatabaseFromWebsiteTest", "Delete existing database items...")
        appContext.database.use{
            delete("MenuItems")
            delete("ItemAllergens")
        }

        println("Creating MenuManager...")
        val mm = MenuManager(appContext)

        Log.d("populateDatabaseFromWebsiteTest", "Getting EVK breakfast menu...")
        val evkBreakfastListEmpty: ArrayList<MenuItem> = mm.getMenu(DiningHallType.EVK, MealType.BREAKFAST)

        // Assert menu is empty
        assertEquals(evkBreakfastListEmpty.size, 0)

        Log.d("populateDatabaseFromWebsiteTest", "Attempting to populate database...")
        mm.populateDatabaseFromWebsite(appContext, date)
        Log.d("populateDatabaseFromWebsiteTest", "Database populated!")

        val evkBreakfastListFull: ArrayList<MenuItem> = mm.getMenu(DiningHallType.EVK, MealType.BREAKFAST)

        // Assert menu is empty
        assertNotEquals(evkBreakfastListFull.size, 0)
        Log.d("populateDatabaseFromWebsiteTest", "Ended!")
    }
}

