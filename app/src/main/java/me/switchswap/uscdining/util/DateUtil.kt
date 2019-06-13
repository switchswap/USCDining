package me.switchswap.uscdining.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.TextView
import me.switchswap.uscdining.R
import java.text.SimpleDateFormat
import java.util.*

class DateUtil {

    /**
     * Converts unix time representation into current readable time
     * @return A time format of MM dd yyyy
     *
     * Converts a date string of format MM/dd/yyyy into a unix time stamp
     *
     * Converts a Calendar to a unix time stamp
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
    fun convertDate(calendar: Calendar): Long? {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return convertDate("${month + 1}/$day/$year")
    }

    /**
     * Read date from shared preferences as a unix timestamp
     * If no activity is given, returns -1
     *
     * @param activity is the activity to call this from
     */
    fun readDate(activity: Activity?): Long {
        if(activity == null) return -1
        val currentDate: Long = convertDate(Calendar.getInstance())!!
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return currentDate
        return sharedPref.getLong(activity.getString(R.string.pref_menu_date), currentDate)
    }

    /**
     * Write date to shared preferences as a unix timestamp
     * If no date is given, no date is written
     *
     * commit() instead of apply() because this should be done synchronously
     */
    @SuppressLint("ApplySharedPref")
    fun writeDate(activity: Activity?, unixTimeStamp: Long?) {
        if(activity == null || unixTimeStamp == null) return

        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putLong(activity.getString(R.string.pref_menu_date), unixTimeStamp)
            commit()
        }
    }
}