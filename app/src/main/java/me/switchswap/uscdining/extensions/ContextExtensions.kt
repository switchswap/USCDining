package me.switchswap.uscdining.extensions

import android.content.Context
import me.switchswap.uscdining.menu.AppDatabase

class ContextExtensions {
    fun Context?.db() = this?.let { AppDatabase.getInstance(it) }
}