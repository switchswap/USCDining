package me.switchswap.uscdining

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import me.switchswap.uscdining.data.*
import models.DiningHallType
import models.ItemType
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class MenuManagerTest {
    private lateinit var db: AppDatabase
    private lateinit var menuDao: MenuDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, AppDatabase::class.java).build()
        menuDao = db.menuDao()

        runBlocking {
            val diningHalls = ArrayList<DiningHall>()
            diningHalls.add(DiningHall(DiningHallType.EVK.id, DiningHallType.EVK.name))
            diningHalls.add(DiningHall(DiningHallType.PARKSIDE.id, DiningHallType.PARKSIDE.name))
            diningHalls.add(DiningHall(DiningHallType.VILLAGE.id, DiningHallType.VILLAGE.name))

            // Insert into database on creation
            menuDao.insertDiningHalls(diningHalls)
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testDbCreation() {
        val diningHalls: List<DiningHall> = menuDao.getDiningHalls()
        assertEquals(3, diningHalls.size)
    }

    @Test
    @Throws(Exception::class)
    fun testInsertItemsFromWeb() {
        val menuManager = MenuManager(menuDao)
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateString = "05/20/2020"
        val date: Date = simpleDateFormat.parse(dateString)!!

        runBlocking {
            menuManager.getMenuFromWeb(date.time)
        }

        val menuItems: List<MenuItemAndAllergens> =
                menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, date.time)

        assertEquals(11, menuItems.size)
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateItemsFromWeb() {
        val menuManager = MenuManager(menuDao)
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateString = "05/20/2020"
        val date: Date = simpleDateFormat.parse(dateString)!!

        // Assert that every time MenuManager retrieves the menu from the internet, it clears the
        // old data before inserting the new data
        for (i in 0 until 2) {
            runBlocking {
                menuManager.getMenuFromWeb(date.time)
            }

            val menuItems: List<MenuItemAndAllergens> =
                    menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, date.time)

            assertEquals(11, menuItems.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDateHasBrunchTrue() {
        val menuManager = MenuManager(menuDao)
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateString = "02/22/2020"
        val date: Date = simpleDateFormat.parse(dateString)!!

        runBlocking {
            menuManager.getMenuFromWeb(date.time)
        }

        assertTrue(menuDao.hallHasBrunch(DiningHallType.PARKSIDE, date.time))
    }

    @Test
    @Throws(Exception::class)
    fun testDateHasBrunchFalse() {
        val menuManager = MenuManager(menuDao)
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val dateString = "02/21/2020"
        val date: Date = simpleDateFormat.parse(dateString)!!

        runBlocking {
            menuManager.getMenuFromWeb(date.time)
        }

        assertFalse(menuDao.hallHasBrunch(DiningHallType.PARKSIDE, date.time))
    }
}
