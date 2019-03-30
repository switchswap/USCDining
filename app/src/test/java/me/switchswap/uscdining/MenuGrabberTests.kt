package me.switchswap.uscdining

import me.switchswap.uscdining.models.DiningHallType
import me.switchswap.uscdining.parser.MenuGrabber
import me.switchswap.uscdining.parser.MenuParser
import org.jsoup.Jsoup
import org.junit.Test

import org.junit.Assert.*
import java.util.*

class MenuGrabberTests {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun buildUrl_isCorrect() {
        val menuParser = MenuParser()
        val date = Date(1548971445000) // Millisecond format for 01/31/2019
        val location: DiningHallType = DiningHallType.PARKSIDE
        val expected = "https://hospitality.usc.edu/residential-dining-menus/?menu_venue=venue-518&menu_date=01/31/2019"
        assertEquals(expected, menuParser.buildUrl(location, date))
    }

    @Test
    fun buildMenu_isCorrect() {
        val menuParser = MenuParser()
        val date = Date(1548971445000) // Millisecond format for 01/31/2019
        val location: DiningHallType = DiningHallType.PARKSIDE
        menuParser.buildMenu(location, date)
    }
}