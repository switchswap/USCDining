package me.switchswap.uscdining.util

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import me.switchswap.uscdining.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @param activity is the activity to call this from
 */
class DateUtil(private val activity: FragmentActivity?) {

    private val sharedPreferences by lazy {
        activity?.getPreferences(Context.MODE_PRIVATE)
    }

    fun subscribe(onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) =
            sharedPreferences?.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

    fun unSubscribe(onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener) =
            sharedPreferences?.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)

    /**
     * Read date from shared preferences as a unix timestamp
     * If no activity is given, returns -1
     *
     */
    fun readDate(): Long {
        if(activity == null) return -1
        val currentDate: Long = convertDate(Calendar.getInstance())!!
        return sharedPreferences?.getLong(activity.getString(R.string.pref_menu_date), currentDate) ?: currentDate
    }

    /**
     * Write date to shared preferences as a unix timestamp
     * If no date is given, no date is written
     *
     * We're using apply because we will depend on subscription to be picked up by our
     * [me.switchswap.uscdining.ui.fragments.MenuFragment]
     */
    fun writeDate(unixTimeStamp: Long?) {
        if(activity == null || unixTimeStamp == null) return

        sharedPreferences?.edit()?.apply {
            putLong(activity.getString(R.string.pref_menu_date), unixTimeStamp)
            apply()
        }
    }

    companion object {

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

        /**
         * [SimpleDateFormat.parse] can produce null so the variable date can be null
         */
        fun convertDate(dateString: String): Long? {
            val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val date = simpleDateFormat.parse(dateString)
            return date?.time
        }

        fun convertDate(calendar: Calendar): Long? {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            return convertDate("${month + 1}/$day/$year")
        }
    }
}