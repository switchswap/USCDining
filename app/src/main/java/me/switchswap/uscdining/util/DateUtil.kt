package me.switchswap.uscdining.util

import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    /**
     * Converts unix time representation into current readable time
     * @return A time format of MM dd yyyy
     *
     * Converts a date string of format MM/dd/yyyy into a unix time stamp
     * @return A unix time stamp
     */
    fun convertDate(unixTimeStamp: Long): String? {
        if (unixTimeStamp != 0L) {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            return dateFormat.format(Date(unixTimeStamp))
        }
        return null
    }
    fun convertDate(dateString: String): Long? {
        val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val date: Date = simpleDateFormat.parse(dateString)
        return date.time
    }

}