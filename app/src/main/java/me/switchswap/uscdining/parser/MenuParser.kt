package me.switchswap.uscdining.parser

import android.annotation.SuppressLint
import me.switchswap.uscdining.models.DiningHallType
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.util.*


/* Class for grabbing and parsing meal data */
class MenuParser{

    fun buildMenu(location: DiningHallType, date: Date){
        val menuHTML = fetchMenu(location, date)
        parseMenu(menuHTML)
    }

    @SuppressLint("SimpleDateFormat")
    fun buildUrl(location: DiningHallType, date: Date):String{
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

    private fun parseMenu(menuHTML: Document){
        // Isolate meal type columns (Breakfast, Brunch, Lunch, Dinner)
        val mealTypeElements = menuHTML.select("div.col-sm-6.col-md-4")
        mealTypeElements.forEachIndexed { _, mealType ->
            val mealTypeTitleElement = mealType.getElementsByClass("menu-venue-title")
            val mealItemElements = mealType.select("ul.menu-item-list > li")
            println(mealTypeTitleElement.text())

            mealItemElements.forEachIndexed { _, item ->
                println(item.ownText())

                // Get item allergens
                val mealItemAllergenElements = item.select("span.fa-allergen-container > i > span")
                mealItemAllergenElements.forEachIndexed{ _, allergen ->
                    print(allergen.text() + " ")
                }
                println()
                println()
            }
        }
    }


}
// INSERT INTO table (id, name, age) VALUES(1, "A", 19) ON DUPLICATE KEY UPDATE name="A", age=19