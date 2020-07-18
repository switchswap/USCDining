package me.switchswap.uscdining


import me.switchswap.uscdining.util.DateUtil
import org.junit.Test
import org.junit.Assert.*

class DateTest {

    @Test
    fun epochToString_Test(){
        val dateString: String? = DateUtil.convertDate(1548921600000)
        assertEquals(dateString, "01/31/2019")
    }

    @Test
    fun stringToEpoch_Test(){
        val unixTimeStamp: Long? = DateUtil.convertDate("01/31/2019")
        assertEquals(unixTimeStamp, 1548921600000)
    }
}