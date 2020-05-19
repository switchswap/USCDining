package me.switchswap.uscdining.menu

import Dining
import models.*
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import java.io.IOException
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/* Class for loading data from database or internet */
class MenuManager(private val database: MenuStorage?) {
    private val dining = Dining()

    /**
     * Get menu from network and update database
     *
     * @throws IOException in the case of a network error
     * @throws IllegalArgumentException in the case of a parsing error
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    fun getMenuFromWeb(date: Long) {
        // Grab dining menu from API
        val diningMenu: DiningMenu = dining.getDiningMenu(Date(date))

        // Insert it into database
        insertItems(diningMenu)
    }

    /**
     * Check if mmenu exists for a given dining hall type and a given date
     */
    fun menuExists(diningHallType: DiningHallType, date: Long): Boolean = database?.use {
        // Get menu items from database
        val query = "SELECT count(*) FROM MenuItems WHERE (itemDate = $date) AND (hallId = ${diningHallType.id})"

        val cursor = rawQuery(query, null)

        var count = 0
        if (cursor.moveToNext()) {
            count += cursor.getInt(cursor.position)
        }
        cursor.close()

        count > 0
    } ?: false

    /**
     * Check if menu exists for a given date
     */
    fun menuExists(date: Long): Boolean = database?.use {
        // Get menu items from database
        val query = "SELECT count(*) FROM MenuItems WHERE (itemDate = $date)"

        val cursor = rawQuery(query, null)

        var count = 0
        if (cursor.moveToNext()) {
            count += cursor.getInt(cursor.position)
        }
        cursor.close()

        count > 0
    } ?: false


    /**
     * Gets and integer representation of the open dining halls
     * Format is EVK Parkside Village
     *
     * @return integer representing the open dining halls where 1 is open and 0 is closed
     */
    fun getOpenDiningHalls(date: Long): Int {
        var openHalls = 0
        if(menuExists(DiningHallType.EVK, date)) openHalls += 100
        if(menuExists(DiningHallType.PARKSIDE, date)) openHalls += 10
        if(menuExists(DiningHallType.VILLAGE, date)) openHalls += 1

        return openHalls
    }

    /**
     * Inserts items into SQLite database
     */
    private fun insertItems(diningMenu: DiningMenu) {
        // Todo: Update this to allow for more dates in the database at a time
        // Delete all values from tables that will be updated
        database?.use {
            delete("MenuItems")
            delete("ItemAllergens")
        }
        insertItems(diningMenu.parkside, DiningHallType.PARKSIDE)
        insertItems(diningMenu.evk, DiningHallType.EVK)
        insertItems(diningMenu.village, DiningHallType.VILLAGE)
    }
    private fun insertItems(hallMenu: HallMenu, diningHallType: DiningHallType) {
        insertItems(hallMenu.breakfast, diningHallType, hallMenu.date)
        insertItems(hallMenu.brunch, diningHallType, hallMenu.date)
        insertItems(hallMenu.lunch, diningHallType, hallMenu.date)
        insertItems(hallMenu.dinner, diningHallType, hallMenu.date)
    }
    private fun insertItems(menuItems: HashMap<String, MenuItem>, diningHallType: DiningHallType, date: Date) {
        // Insert each menu item
        menuItems.forEach { menuItem ->
            database?.use {
                val itemId = insert("MenuItems", "itemName" to menuItem.value.itemName,
                        "hallId" to diningHallType.id, "itemType" to menuItem, "itemDate" to date.time)

                menuItem.value.allergens.forEach { allergen ->
                    insert("ItemAllergens", "allergenName" to allergen, "menuItemId" to itemId)
                }
                println("Here")
            }
        }
    }
}

