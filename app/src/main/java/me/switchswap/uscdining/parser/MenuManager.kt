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
class MenuManager(private val context: Context){

    fun getMenu(diningHallType: DiningHallType, mealType: MealType) : ArrayList<MenuItem> {
        return getMenuFromDatabase(diningHallType, mealType)
        // TODO: Possibly allow getting by date to cache past menus
    }

    /**
     * Retrieves menu from the USC residential dining halls website and populates SQLite database
     * @param context is for accessing the SQLite database
     * @param date specifies which date to get menu for
     */
    fun populateDatabaseFromWebsite(context: Context, date: Date): Boolean {
        val menuParser = MenuParser()
        val menuItems = menuParser.getMenuItems(date)

        return if(menuItems != null) {
            // Insert items into database for easy manipulation
            insertItems(menuItems)
            true
        }
        else {
            // Something went wrong!
            false
        }
    }

    /**
     * Retrieves an ArrayList of MenuItems from SQLite database
     * @param diningHallType specifies diningHallType
     * @param mealType specifies mealType
     */
    private fun getMenuFromDatabase(diningHallType: DiningHallType, mealType: MealType) : ArrayList<MenuItem> = context.database.use {
        // Get menu items from database
        val menuItems = ArrayList<MenuItem>()
        select("MenuItems", "id", "itemName")
            .whereArgs("(hallId = {hallId}) and (mealType = {mealType})",
                    "hallId" to diningHallType.id, "mealType" to mealType.typeName)
            .parseList(object: MapRowParser<List<MenuItem>> {
                override fun parseRow(columns: Map<String, Any?>): List<MenuItem> {
                    val itemId = columns.getValue("id")!!
                    val itemName = columns.getValue("itemName")

                    // Get allergens from database for specific menu item
                    val itemAllergens = ArrayList<String>()
                    select("ItemAllergens", "allergenName")
                        .whereArgs("menuItemId = {id}", "id" to itemId)
                        .parseList(object: MapRowParser<List<String>> {
                            override fun parseRow(columns: Map<String, Any?>): List<String>{
                                val allergenName = columns.getValue("allergenName")
                                itemAllergens.add(allergenName.toString())
                                return itemAllergens
                            }
                        }
                    )

                    // Append menuItems to list and return
                    val menuItem = MenuItem(itemName.toString(), itemAllergens, mealType, diningHallType)
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
    fun insertItems(menuItems: ArrayList<MenuItem>) {
        // Delete all values from tables that will be updated
        context.database.use{
            delete("MenuItems")
            delete("ItemAllergens")
        }

        // Insert each menu item
        menuItems.forEach{ menuItem ->
            context.database.use{
                val itemId = insert("MenuItems", "itemName" to menuItem.itemName,
                        "hallId" to menuItem.diningHallType.id, "mealType" to menuItem.mealType.typeName)

                menuItem.allergens.forEach{ allergen ->
                    insert("ItemAllergens", "allergenName" to allergen, "menuItemId" to itemId)
                }
            }
        }
    }
}

