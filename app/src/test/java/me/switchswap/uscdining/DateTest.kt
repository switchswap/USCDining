package me.switchswap.uscdining


import me.switchswap.uscdining.util.DateUtil
import org.junit.Test
import org.junit.Assert.*

class DateTest {
    private val dateUtil = DateUtil()

    @Test
    fun epochToString_Test(){
        val dateString: String? = dateUtil.convertDate(1548921600000)
        assertEquals(dateString, "01/31/2019")
    }

    @Test
    fun stringToEpoch_Test(){
        val unixTimeStamp: Long? = dateUtil.convertDate("01/31/2019")
        assertEquals(unixTimeStamp, 1548921600000)
    }
}