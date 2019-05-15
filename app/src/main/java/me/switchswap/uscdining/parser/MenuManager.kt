package me.switchswap.uscdining.parser

import android.content.Context
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.models.MenuItem
import org.jetbrains.anko.db.*
import kotlin.collections.ArrayList

/* Class for loading data from database or internet */
class MenuManager(val context: Context){

    fun getMenuFromDatabase(diningHall: DiningHallType, mealType: MealType) : ArrayList<MenuItem> = context.database.use {
        // Get menu items from database
        val menuItems = ArrayList<MenuItem>()
        select("MenuItems", "id", "title")
            .whereArgs("(DiningHallType = {diningHall}) and (mealType = {mealType})",
                    "diningHall" to diningHall.id, "mealType" to mealType.typeName)
            .parseList(object: MapRowParser<List<MenuItem>> {
                override fun parseRow(columns: Map<String, Any?>): List<MenuItem> {
                    val itemId = columns.getValue("id")!!
                    val itemName = columns.getValue("title")

                    // Get allergens from database for specific menu item
                    val itemAllergens = ArrayList<String>()
                    select("ItemAllergens", "allergenName")
                        .whereArgs("mealId = {id}", "id" to itemId)
                        .parseList(object: MapRowParser<List<String>> {
                            override fun parseRow(columns: Map<String, Any?>): List<String>{
                                val allergenName = columns.getValue("allergenName")
                                itemAllergens.add(allergenName.toString())
                                return itemAllergens
                            }
                        }
                    )

                    // Append menuItems to list and return
                    val menuItem = MenuItem(itemName.toString(), itemAllergens, mealType)
                    menuItems.add(menuItem)
                    return menuItems
                }
            }
        )
        menuItems
    }

    fun insertItems(menuItems: ArrayList<MenuItem>, hallType: DiningHallType){
        // Drop tables that will be updated
        context.database.use{
            delete("MenuItems")
            delete("ItemAllergens")
        }

        // Insert each menu item
        menuItems.forEach{ menuItem ->
            context.database.use{
                val itemId = insert("MenuItems", "itemName" to menuItem.itemName,
                        "hallId" to hallType.id, "mealType" to menuItem.mealType)

                menuItem.allergens.forEach{ allergen ->
                    insert("ItemAllergens", "allergenName" to allergen, "mealId" to itemId)
                }
            }
        }
    }
}

