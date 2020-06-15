package me.switchswap.uscdining.ui.dialog

import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat

class SimpleDialogPreferenceCompat: PreferenceDialogFragmentCompat() {
    lateinit var positiveResult: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        positiveResult = (preference as SimpleDialogPreference).positiveResult
        super.onCreate(savedInstanceState)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            positiveResult()
        }
    }

    companion object {
        val TAG = SimpleDialogPreferenceCompat::class.java.simpleName

        fun newInstance(key: String): SimpleDialogPreferenceCompat {
            val fragment = SimpleDialogPreferenceCompat()
            val bundle = Bundle(1)
            bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = bundle
            return fragment
        }
    }
}