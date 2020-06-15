package me.switchswap.uscdining.ui.dialog

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference

class SimpleDialogPreference(context: Context?, attrs: AttributeSet?): DialogPreference(context, attrs) {
    lateinit var positiveResult: () -> Unit
}
