package me.switchswap.uscdining.parser

import android.content.Context
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealTime
import me.switchswap.uscdining.models.MenuItem
import org.jetbrains.anko.db.MapRowParser
import org.jetbrains.anko.db.select
import java.util.*
import kotlin.collections.ArrayList

/* Class for loading data from database or internet */
class MenuGrabber(val context: Context){

    fun getMenu(diningHall: DiningHallType, mealTime: MealTime) : ArrayList<MenuItem> = context.database.use {
        // Get menu items from database
        val menuItems = ArrayList<MenuItem>()
        select("MealItems", "id", "title")
            .whereArgs("(DiningHallType = {diningHall}) and (mealTime = {mealTime})",
                    "diningHall" to diningHall.id, "mealTime" to mealTime.timeName)
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
                    val menuItem = MenuItem(itemName.toString(), itemAllergens)
                    menuItems.add(menuItem)
                    return menuItems
                }
            }
        )
        menuItems
    }
}

