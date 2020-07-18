package me.switchswap.uscdining.extensions

import android.content.Context
import me.switchswap.uscdining.data.AppDatabase

fun Context.db() = this.let { AppDatabase.getInstance(it) }