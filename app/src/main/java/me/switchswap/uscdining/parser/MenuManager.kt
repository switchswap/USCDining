package me.switchswap.uscdining.parser

import android.content.Context
import android.util.Log
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.models.MenuItem
import org.jetbrains.anko.db.*
import org.jetbrains.anko.longToast
import java.util.*
import kotlin.collections.ArrayList

/* Class for loading data from database or internet */
class MenuManager(private val context: Context) {

    fun getMenu(diningHallType: DiningHallType, mealType: MealType, date: Long): ArrayList<MenuItem> {
        return getMenuFromDatabase(diningHallType, mealType, date)
        //todo: This wrapper function may not be necessary
    }

    fun checkMenuExists(diningHallType: DiningHallType?, date: Long): Boolean = context.database.use {
        // Get menu items from database
        val query = if (diningHallType == null) "SELECT count(*) FROM MenuItems WHERE (itemDate = $date)"
        else "SELECT count(*) FROM MenuItems WHERE (itemDate = $date) AND (hallId = ${diningHallType.id})"

        val cursor = rawQuery(query, null)

        var count = 0
        if (cursor.moveToNext()) {
            count = cursor.getInt(cursor.position)
        }
        cursor.close()

        count > 0
    }

    /**
     * Gets and integer representation of the open dining halls
     * Format is EVK Parkside Village
     * @return integer representing the open dining halls where 1 is open and 0 is closed
     */
    fun getOpenDiningHalls(date: Long): Int {
        var openHalls = 0
        if(checkMenuExists(DiningHallType.EVK, date)) openHalls += 100
        if(checkMenuExists(DiningHallType.PARKSIDE, date)) openHalls += 10
        if(checkMenuExists(DiningHallType.VILLAGE, date)) openHalls += 1

        return openHalls
    }


    /**
     * Retrieves menu from the USC residential dining halls website and populates SQLite database
     * @param date specifies which date to get menu for
     *
     * @return success or failure
     */
    fun populateDatabaseFromWebsite(date: Date): Boolean {
        val menuParser = MenuParser()
        val menuItems = menuParser.getMenuItems(date)

        return if (menuItems != null) {
            // Insert items into database for easy manipulation
            insertItems(menuItems)
            true
        } else {
            // Something went wrong!
            false
        }
    }

    /**
     * Retrieves an ArrayList of MenuItems from SQLite database
     * @param diningHallType specifies diningHallType
     * @param mealType specifies mealType
     */
    private fun getMenuFromDatabase(diningHallType: DiningHallType, mealType: MealType, date: Long): ArrayList<MenuItem> = context.database.use {
        // Get menu items from database
        val menuItems = ArrayList<MenuItem>()
        select("MenuItems", "id", "itemName")
                .whereArgs("(hallId = {hallId}) and (mealType = {mealType}) and (itemDate = {itemDate})",
                        "hallId" to diningHallType.id, "mealType" to mealType.typeName, "itemDate" to date)
                .parseList(object : MapRowParser<List<MenuItem>> {
                    override fun parseRow(columns: Map<String, Any?>): List<MenuItem> {
                        val itemId = columns.getValue("id")!!
                        val itemName = columns.getValue("itemName")

                        // Get allergens from database for specific menu item
                        val itemAllergens = ArrayList<String>()
                        select("ItemAllergens", "allergenName")
                                .whereArgs("menuItemId = {id}", "id" to itemId)
                                .parseList(object : MapRowParser<List<String>> {
                                    override fun parseRow(columns: Map<String, Any?>): List<String> {
                                        val allergenName = columns.getValue("allergenName")
                                        itemAllergens.add(allergenName.toString())
                                        return itemAllergens
                                    }
                                }
                                )

                        // Append menuItems to list and return
                        val menuItem = MenuItem(itemName.toString(), itemAllergens, mealType, diningHallType, date)
                        menuItems.add(menuItem)
                        return menuItems
                    }
                }
                )
        menuItems
    }

    /**
     * Inserts items into SQLite database
     * @param menuItems is what gets inserted
     */
    private fun insertItems(menuItems: ArrayList<MenuItem>) {
        // Delete all values from tables that will be updated
        context.database.use {
            delete("MenuItems")
            delete("ItemAllergens")
        }

        // Insert each menu item
        menuItems.forEach { menuItem ->
            context.database.use {
                val itemId = insert("MenuItems", "itemName" to menuItem.itemName,
                        "hallId" to menuItem.diningHallType.id, "mealType" to menuItem.mealType.typeName, "itemDate" to menuItem.date)

                menuItem.allergens.forEach { allergen ->
                    insert("ItemAllergens", "allergenName" to allergen, "menuItemId" to itemId)
                }
            }
        }
    }
}

