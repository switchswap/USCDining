package me.switchswap.uscdining.menu

import Dining
import android.content.Context
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import models.*
import models.MenuItem
import org.jetbrains.anko.longToast
import me.switchswap.uscdining.menu.MenuItem as DatabaseMenuItem
import java.util.*
import kotlin.collections.HashMap

/* Class for loading data from database or internet */
class MenuManager(private val context: Context, private val menuDao: MenuDao = AppDatabase.getInstance(context).menuDao()) {

    /**
     * Get menu from network and update database
     */
    suspend fun getMenuFromWeb(date: Long) {
        val dining = Dining()
        kotlin.runCatching {
            // Grab dining menu from API
            val diningMenu: DiningMenu = dining.getDiningMenu(Date(date))
            // Insert it into database
            insertItems(diningMenu)
        }.getOrElse {
            // Todo: Move these comments somewhere reasonable
            // @throws IOException in the case of a network error
            // @throws IllegalArgumentException in the case of a parsing error
            withContext(Main) {
                context.longToast("Error fetching menu!")
            }
        }
    }

    /**
     * Inserts items into SQLite database
     */
    private suspend fun insertItems(diningMenu: DiningMenu) {
        // Todo: Update this to allow for more dates in the database at a time
        // Delete all values from tables that will be updated
        menuDao.dropMenuItems()

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
            val databaseMenuItem = DatabaseMenuItem(0, menuItem.itemName,
                    menuItem.itemType.typeName, menuItem.itemCategory, date.time, diningHallType.id)

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

    fun getMenuFromDatabase(diningHallType: DiningHallType, itemType: ItemType, date: Long): List<MenuItem> {
        // Todo: Complete this function
        return ArrayList()
    }

    companion object {
        val TAG = MenuManager::class.java.simpleName
    }
}

