package me.switchswap.uscdining.ui.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import android.widget.TextView
import me.switchswap.uscdining.R
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var calendar: Calendar
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        calendar = Calendar.getInstance()

        // Get current data
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(activity, this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        // Change toolbar date
        val toolbarDate = activity!!.findViewById<TextView>(R.id.toolbar_date)
        toolbarDate.text = getString(R.string.date_string, month + 1, day, year % 100)

        // Populate menu

    }
}