package me.switchswap.uscdining.data

import android.util.Log
import androidx.lifecycle.LiveData
import me.switchswap.diningmenu.Dining
import me.switchswap.diningmenu.models.*
import me.switchswap.uscdining.data.MenuItem as DatabaseMenuItem
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/* Class for loading data from database or internet */
class MenuRepository(private val menuDao: MenuDao) {
    /**
     * Inserts items into SQLite database
     */
    private suspend fun insertItems(diningMenu: DiningMenu, cacheData: Boolean) {
        // Delete all values from tables that will be updated
        if (cacheData) {
            menuDao.dropMenuItems(diningMenu.date.time)
        }
        else {
            menuDao.dropAllMenuItems()
        }

        insertItems(diningMenu.parkside, DiningHallType.PARKSIDE)
        insertItems(diningMenu.evk, DiningHallType.EVK)
        insertItems(diningMenu.village, DiningHallType.VILLAGE)
    }

    private suspend fun insertItems(hallMenu: HallMenu, diningHallType: DiningHallType) {
        insertItems(hallMenu.breakfast, diningHallType, hallMenu.date)
        insertItems(hallMenu.brunch, diningHallType, hallMenu.date)
        insertItems(hallMenu.lunch, diningHallType, hallMenu.date)
        insertItems(hallMenu.dinner, diningHallType, hallMenu.date)
    }

    private suspend fun insertItems(menuItems: HashMap<String, MenuItem>, diningHallType: DiningHallType, date: Date) {
        // Insert each menu item
        menuItems.forEach { item ->
            val menuItem = item.value

            // Insert item into db
            // DatabaseMenuItem is the MenuItem entity imported with a different name to avoid conflicts
            val databaseMenuItem = DatabaseMenuItem(0, menuItem.itemName,
                    menuItem.itemType.name, menuItem.itemCategory, date.time, diningHallType.id)

            // Get id of inserted menu item
            val menuItemId: Int = menuDao.insertMenuItem(databaseMenuItem).toInt()

            // Build list of allergens
            val allergens: ArrayList<Allergen> = ArrayList()
            menuItem.allergens.forEach { allergen ->
                allergens.add(Allergen(0, allergen, menuItemId))
            }

            // Insert allergens into db
            menuDao.insertAllergens(allergens)
        }
    }

    /**
     * Get list of MenuItems from the database
     */
    fun getMenuFromDatabase(diningHallType: DiningHallType, itemType: ItemType, date: Long): LiveData<List<MenuItemAndAllergens>> {
        return menuDao.getMenuItems(diningHallType, itemType, date)
    }

    /**
     * Get menu from network and update database
     */
    suspend fun getMenuFromWeb(date: Long, cacheData: Boolean) {
        val dining = Dining()
        Log.d(TAG, "Fetching menu from web!")
        kotlin.runCatching {
            // Grab dining menu from API
            val diningMenu: DiningMenu = dining.getDiningMenu(Date(date))
            Log.d(TAG, "Retrieved menu for date: ${diningMenu.date}")

            // Insert it into database
            insertItems(diningMenu, cacheData)
        }.onFailure {
            Log.e(TAG, "Error fetching menu from web!", it)
        }
    }


    companion object {
        val TAG = MenuRepository::class.java.simpleName
    }
}

