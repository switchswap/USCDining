package me.switchswap.uscdining

import android.util.Log
import me.switchswap.uscdining.models.DiningHallType
import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun buildUrl_isCorrect(){
        val date = Date(1548971445000);
        val diningHallType: DiningHallType = DiningHallType.PARKSIDE

        val expected: String = "https://hospitality.usc.edu/residential-dining-menus/?menu_venue=venue-518&menu_date=01/31/2019"
        assertEquals("03/02/2019", answer)
    }
}
