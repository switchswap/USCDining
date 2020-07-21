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
class MenuRepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var context: Context
    private lateinit var menuDao: MenuDao
    private lateinit var menuRepository: MenuRepository

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, AppDatabase::class.java).build()
        menuDao = db.menuDao()
        menuRepository = MenuRepository(menuDao)

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
        val date: Long = dateStringToLong("05/20/2020")

        runBlocking {
            menuRepository.getMenuFromWeb(date, false)
        }

        val menuItems: List<MenuItemAndAllergens> =
                menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, date)

        assertEquals(11, menuItems.size)
    }

    @Test
    @Throws(Exception::class)
    fun testUpdateItemsFromWeb() {
        val date: Long = dateStringToLong("05/20/2020")

        // Assert that every time MenuManager retrieves the menu from the internet, it clears the
        // old data from the given date before inserting the new data
        for (i in 0 until 2) {
            runBlocking {
                // Even with data caching enabled, the existing data for the date should be deleted
                menuRepository.getMenuFromWeb(date, true)
            }

            val menuItems: List<MenuItemAndAllergens> =
                    menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, date)

            assertEquals(11, menuItems.size)
        }
    }

    @Test
    @Throws(Exception::class)
    fun testDateHasBrunchTrue() {
        val date: Long = dateStringToLong("02/22/2020")

        runBlocking {
            menuRepository.getMenuFromWeb(date, false)
        }

        assertTrue(menuDao.hallHasBrunch(DiningHallType.PARKSIDE, date))
    }

    @Test
    @Throws(Exception::class)
    fun testDateHasBrunchFalse() {
        val date: Long = dateStringToLong("02/21/2020")

        runBlocking {
            menuRepository.getMenuFromWeb(date, false)
        }

        assertFalse(menuDao.hallHasBrunch(DiningHallType.PARKSIDE, date))
    }

    @Test
    @Throws(Exception::class)
    fun testDataCachingOn() {
        val day1: Long = dateStringToLong("05/20/2020")
        val day2: Long = dateStringToLong("05/21/2020")
        runBlocking {
            menuRepository.getMenuFromWeb(day1, true)
            menuRepository.getMenuFromWeb(day2, true)
        }

        val day1Items: List<MenuItemAndAllergens> =
                menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, day1)
        assertEquals(11, day1Items.size)

        val day2Items: List<MenuItemAndAllergens> =
                menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, day2)
        assertEquals(11, day2Items.size)
    }

    @Test
    @Throws(Exception::class)
    fun testDataCachingOff() {
        val day1: Long = dateStringToLong("05/20/2020")
        val day2: Long = dateStringToLong("05/21/2020")
        runBlocking {
            menuRepository.getMenuFromWeb(day1, false)
            menuRepository.getMenuFromWeb(day2, false)
        }

        val day1Items: List<MenuItemAndAllergens> =
                menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, day1)
        assertEquals(0, day1Items.size)

        val day2Items: List<MenuItemAndAllergens> =
                menuDao.getMenuItems(DiningHallType.VILLAGE, ItemType.BREAKFAST.typeName, day2)
        assertEquals(11, day2Items.size)
    }

    private fun dateStringToLong(dateString: String): Long {
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return simpleDateFormat.parse(dateString)!!.time
    }
}
