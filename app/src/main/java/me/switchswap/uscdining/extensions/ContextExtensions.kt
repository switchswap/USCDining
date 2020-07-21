package me.switchswap.uscdining.extensions

import android.content.Context
import android.widget.Toast
import me.switchswap.uscdining.data.AppDatabase

fun Context.db() = this.let { AppDatabase.getInstance(it) }

fun Context.longToast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}

fun Context.toast(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}