package me.switchswap.uscdining

import android.content.Context
import android.util.Log
import android.view.Menu
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import me.switchswap.uscdining.menu.*
import me.switchswap.uscdining.ui.fragments.MenuFragment
import models.ItemType
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4ClassRunner::class)
class MenuDatabaseTests {
    private lateinit var db: AppDatabase
    private lateinit var menuDao: MenuDao

    private val diningHall = DiningHall(1L, "TestHall")
    private val menuItemA = MenuItem(0, "Soup", ItemType.BREAKFAST.typeName, "Temp", 1L, 1L)
    private val menuItemB = MenuItem(0, "Bread", ItemType.LUNCH.typeName, "Temp", 1L, 1L)
    private val menuItemC = MenuItem(0, "Cheese", ItemType.DINNER.typeName, "Temp", 1L, 1L)


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, AppDatabase::class.java).build()
        menuDao = db.menuDao()

        runBlocking {
            menuDao.insertDiningHalls(listOf(diningHall))
        }
    }


    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testDbCreationTrue() {
        val diningHalls: List<DiningHall> = menuDao.getDiningHalls()
        var found: Boolean = false
        for (diningHall: DiningHall in diningHalls) {
            if (diningHall.id == 1L) {
                found = true
                break
            }
        }
        assertTrue(found)
    }

    @Test
    @Throws(Exception::class)
    fun testDbCreationFalse() {
        val diningHalls: List<DiningHall> = menuDao.getDiningHalls()
        var found: Boolean = false
        for (diningHall: DiningHall in diningHalls) {
            if (diningHall.id == 5L) {
                found = true
                break
            }
        }
        assertFalse(found)
    }

    @Test
    @Throws(Exception::class)
    fun testMenuItemInsert() {
        runBlocking {
            menuDao.insertMenuItem(menuItemA)
        }

        // Get all MenuItems from db along with their corresponding allergens
        val menuItemAndAllergens: List<MenuItemAndAllergens> = menuDao.getMenuItems(1, ItemType.BREAKFAST.typeName, 1L)
        assertEquals(1, menuItemAndAllergens.size)
        assertEquals("Soup", menuItemAndAllergens[0].menuItem.name)
        assertEquals(ItemType.BREAKFAST.typeName, menuItemAndAllergens[0].menuItem.type)
        assertEquals("Temp", menuItemAndAllergens[0].menuItem.category)
        assertEquals(1L, menuItemAndAllergens[0].menuItem.date)
        assertEquals(1L, menuItemAndAllergens[0].menuItem.hallId)

        // There should be no allergens returned since there were none inserted
        assertEquals(0, menuItemAndAllergens[0].allergens.size)
    }

    @Test
    @Throws(Exception::class)
    fun testAllergenInsert() {
        runBlocking {
            val menuItemId: Int = menuDao.insertMenuItem(menuItemA).toInt()

            menuDao.insertAllergens(listOf(Allergen(0,"Chicken",menuItemId),
                    Allergen(0,"Eggs", menuItemId),
                    Allergen(0,"Dairy", menuItemId)))
        }

        // Get all MenuItems from db along with their corresponding allergens
        val menuItemAndAllergens: List<MenuItemAndAllergens> = menuDao.getMenuItems(1, ItemType.BREAKFAST.typeName, 1L)
        assertEquals(1, menuItemAndAllergens.size)

        assertEquals(3, menuItemAndAllergens[0].allergens.size)
    }

    companion object {
        val TAG = MenuDatabaseTests::class.java.simpleName
    }
}
