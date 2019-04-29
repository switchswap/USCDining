package me.switchswap.uscdining.parser

import android.annotation.SuppressLint
import android.content.Context
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.models.MenuItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/* Class for grabbing and parsing meal data */
class MenuParser(private val context: Context){

    fun buildMenu(location: DiningHallType, date: Date){
        val menuHTML = fetchMenu(location, date)

        // Parse into menu items
        val menuItems = parseMenu(menuHTML)

        // Insert items into database for easy manipulation
        val menuManager = MenuManager(context)
        menuManager.insertItems(menuItems, location)
    }

    @SuppressLint("SimpleDateFormat")
    fun buildUrl(location: DiningHallType, date: Date): String{
        // Format date
        val dateString: String = SimpleDateFormat("MM/dd/yyyy").format(date)

        // Build url
        return "https://hospitality.usc.edu/residential-dining-menus/" +
                "?menu_venue=venue-${location.id}" + "&menu_date=$dateString"
    }

    // Fetch menu HTML from url
    private fun fetchMenu(location: DiningHallType, date: Date): Document{
        // Fetch menu html
        val url: String = buildUrl(location, date)
        return Jsoup.connect(url).get()
    }


    // Parse menu HTML into MenuItem objects
    private fun parseMenu(menuHTML: Document): ArrayList<MenuItem>{
        // Array of all the mealItems for each meal type
        val mealItems = ArrayList<MenuItem>()

        // Isolate meal type columns (Breakfast, Brunch, Lunch, Dinner)
        val mealTypeElements = menuHTML.select("div.col-sm-6.col-md-4")
        mealTypeElements.forEachIndexed { _, mealType ->

            // Parse values from html data into array
            val mealTypeTitleElement = mealType.getElementsByClass("menu-venue-title")
            val mealItemElements = mealType.select("ul.menu-item-list > li")

            mealItemElements.forEachIndexed { _, item ->
                val itemName = item.ownText()
                val allergens = ArrayList<String>()

                // Get item allergens and add them to list
                val mealItemAllergenElements = item.select("span.fa-allergen-container > i > span")
                mealItemAllergenElements.forEachIndexed { _, allergen ->
                    allergens.add(allergen.text())
                }
                mealItems.add(MenuItem(itemName, allergens, MealType.valueOf(mealTypeTitleElement.text().toUpperCase())))
            }
        }
        return mealItems
    }
}