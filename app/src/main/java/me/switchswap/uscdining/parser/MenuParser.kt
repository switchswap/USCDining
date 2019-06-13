package me.switchswap.uscdining.parser

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.VisibleForTesting
import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.models.MealType
import me.switchswap.uscdining.models.MenuItem
import me.switchswap.uscdining.util.DateUtil
import org.jetbrains.anko.toast
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/* Class for grabbing and parsing meal data */
class MenuParser {

    /**
     * Scrapes menu data from website and builds menu data
     * Then populates the sqlite database with this menu data
     * @param context is to access the sqlite database
     * @param date specifies which day's menu data to return
     * @return Returns a list of [MenuItem] or null
     */
    fun getMenuItems(date: Date): ArrayList<MenuItem>? {
        // Fetch html
        val menuHTML: Document? = fetchMenu(date)

        return if(menuHTML != null){
            // Parse into menu items
            val menuItems: ArrayList<MenuItem> = parseMenu(menuHTML, date)
            menuItems
        }
        else{
            // Something went wrong!
            null
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun buildUrl(date: Date): String{
        // Format date
        var dateString: String = SimpleDateFormat("MM/dd/yyyy").format(date)
        // Remove '0' from start if applicable since this breaks the link otherwise
        dateString = dateString.removePrefix("0")

        // Build url
        return "https://hospitality.usc.edu/residential-dining-menus/?menu_date=$dateString"
    }

    // Fetch menu HTML from url
    private fun fetchMenu(date: Date) : Document? {
        // Fetch menu html
        val url: String = buildUrl(date)
        return try {
            val html = Jsoup.connect(url).get()
            html
        }
        catch (e: Exception){
            Log.d("fetchMenu", e.message)
            null
        }
    }

    // Parse menu HTML into MenuItem objects
    private fun parseMenu(menuHTML: Document, date: Date): ArrayList<MenuItem> {
        val menuItems : ArrayList<MenuItem> = ArrayList()

        // Get the meal types (Breakfast, Brunch, Lunch, and Dinner)
        val mealTypeElements = menuHTML.select("div.hsp-accordian-container")
        mealTypeElements.forEachIndexed { _, mealTypeElement ->
            val mealTypeNameElement = mealTypeElement.select("h2 > span.fw-accordion-title-inner").first()

            // For each meal type, get each dining hall
            val diningHallElements = mealTypeElement.select("div.col-sm-6.col-md-4")
            diningHallElements.forEachIndexed { _, diningHallElement ->
                val diningHallNameElement = diningHallElement.select("h3.menu-venue-title")

                //For each dining hall, get all items in menu
                val menuItemElements = diningHallElement.select("ul.menu-item-list > li")
                menuItemElements.forEachIndexed { _, menuItemElement ->
                    val itemName = menuItemElement.ownText()

                    // For each item, get all allergens
                    val allergens = ArrayList<String>()
                    val mealItemAllergenElements = menuItemElement.select("span.fa-allergen-container > i > span")
                    mealItemAllergenElements.forEachIndexed { _, menuItemAllergenElement ->
                        allergens.add(menuItemAllergenElement.text())
                    }

                    menuItems.add(MenuItem(itemName, allergens,
                            getMealTypeFromString(mealTypeNameElement.text().toUpperCase())!!,
                            getDiningHallTypeFromString(diningHallNameElement.text())!!,
                            date.time))
                }
            }
        }
        return menuItems
    }

    private fun getMealTypeFromString(str: String) : MealType? {
        return when {
            str.contains("BREAKFAST") -> MealType.BREAKFAST
            str.contains("BRUNCH") -> MealType.BRUNCH
            str.contains("LUNCH") -> MealType.LUNCH
            str.contains("DINNER") -> MealType.DINNER
            else -> null
        }
    }

    private fun getDiningHallTypeFromString(str: String) : DiningHallType? {
        return when {
            str.contains("Everybody's Kitchen") -> DiningHallType.EVK
            str.contains("Parkside Restaurant & Grill") -> DiningHallType.PARKSIDE
            str.contains("USC Village Dining Hall") -> DiningHallType.VILLAGE
            else -> null
        }
    }
}