package me.switchswap.uscdining.parser

import models.*
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.delete
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/* Class for loading data from database or internet */
class MenuManager(private val database: MenuStorage?) {

    fun getMenu(diningHallType: DiningHallType, itemType: ItemType, date: Long): List<MenuItem> {
        return getMenuFromDatabase(diningHallType, itemType, date)
    }

    /**
     * Checks if menu for given dining hall exists for a given date
     *
     */
    private fun menuExists(diningHallType: DiningHallType, date: Long): Boolean = database?.use {
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
     * Gets and integer representation of the open dining halls
     * Format is EVK Parkside Village
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
     * Retrieves an ArrayList of MenuItems from SQLite database for a given dining hall, item type,
     * and date
     *
     * @param diningHallType specifies diningHallType
     * @param mealType specifies mealType
     */
    private fun getMenuFromDatabase(diningHallType: DiningHallType, itemType: ItemType, date: Long): List<MenuItem> = database?.use {
        // Get menu items from database
        val menuItems = ArrayList<MenuItem>()
        select("MenuItems", "id", "itemName")
                .whereArgs("(hallId = {hallId}) and (itemType = {itemType}) and (itemDate = {itemDate})",
                        "hallId" to diningHallType.id, "itemType" to itemType.typeName, "itemDate" to date)
                .parseList(object : MapRowParser<List<MenuItem>> {
                    override fun parseRow(columns: Map<String, Any?>): List<MenuItem> {
                        val itemId: Int = columns.getValue("id")!! as Int // itemId should always exist
                        val itemName: String = columns.getValue("itemName") as String
                        val itemCategory: String = columns.getValue("itemCategory") as String

                        // Get allergens from database for specific menu item
                        val itemAllergens = HashSet<String>()
                        select("ItemAllergens", "allergenName")
                                .whereArgs("menuItemId = {id}", "id" to itemId)
                                .parseList(object : MapRowParser<List<String>> {
                                    // Todo: Remove ignored return value
                                    override fun parseRow(columns: Map<String, Any?>): List<String> {
                                        val allergenName: String = columns.getValue("allergenName") as String
                                        itemAllergens.add(allergenName)
                                        return ArrayList()
                                    }
                                })

                        // Append menuItems to list and return
                        val menuItem = MenuItem(itemName, itemAllergens, itemType, itemCategory)
                        menuItems.add(menuItem)
                        return menuItems
                    }
                })
        menuItems
    } ?: emptyList<MenuItem>()

    /**
     * Inserts items into SQLite database
     * @param menuItems is what gets inserted
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
            }
        }
    }
}

