package me.switchswap.uscdining.ui.fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import android.widget.DatePicker
import android.widget.TextView
import me.switchswap.uscdining.R
import me.switchswap.uscdining.parser.MenuManager
import me.switchswap.uscdining.ui.activities.MainActivity
import me.switchswap.uscdining.util.DateUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get current data
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        // Change toolbar date
        val toolbarDate : TextView? = activity?.findViewById(R.id.toolbar_date)
        toolbarDate?.text = getString(R.string.date_string, month + 1, day, year % 100)

        // Update shared preference
        val dateUtil = DateUtil()
        val unixTimeStamp: Long = dateUtil.convertDate("${month + 1}/$day/$year")!!

        val sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPreferences.edit()) {
            putLong(getString(R.string.pref_menu_date), unixTimeStamp)
            commit()
        }

        // Populate database from website if needed
    }
}