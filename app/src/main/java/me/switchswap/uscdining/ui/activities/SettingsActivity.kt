package me.switchswap.uscdining.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.switchswap.uscdining.BuildConfig
import me.switchswap.uscdining.R
import me.switchswap.uscdining.extensions.db
import me.switchswap.uscdining.extensions.longToast
import me.switchswap.uscdining.ui.dialog.SimpleDialogPreference
import me.switchswap.uscdining.ui.dialog.SimpleDialogPreferenceCompat
import me.switchswap.uscdining.util.DateUtil

private const val TITLE_TAG = "settingsActivityTitle"

class SettingsActivity : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, HeaderFragment())
                    .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title_activity_settings)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }

        // Else if we're at the root settings page
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onPreferenceStartFragment(
            caller: PreferenceFragmentCompat,
            pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment
        ).apply {
            arguments = args
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit()
        title = pref.title
        return true
    }

    class HeaderFragment : PreferenceFragmentCompat() {
        private var presses: Int = 0

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.header_preferences, rootKey)

            // Set version number
            val version = findPreference<Preference>(getString(R.string.pref_version))
            version?.apply {
                summary = BuildConfig.VERSION_NAME

                // Build time easter egg
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    if (presses == 5) {
                        context.longToast("Build time: ${DateUtil.getTimeStamp(BuildConfig.TIMESTAMP)}")
                    } else {
                        presses++
                    }
                    true
                }
            }

            val clearCache: SimpleDialogPreference = findPreference<Preference>(getString(R.string.pref_clear_cache)) as SimpleDialogPreference
            clearCache.apply {
                positiveResult = {
                    CoroutineScope(IO).launch {
                        context.db().menuDao().dropAllMenuItems()

                        withContext(Main){
                            context.longToast("Cleared!")
                        }
                    }
                }
            }

            val donate: SimpleDialogPreference = findPreference<Preference>(getString(R.string.pref_donate)) as SimpleDialogPreference
            donate.apply {
                positiveResult = {}
                negativeButtonText = ""
            }
        }

        override fun onDisplayPreferenceDialog(preference: Preference?) {
            val simpleDialogPreference = preference as? SimpleDialogPreference

            // If preference is a [SimpleDialogPreference], show the appropriate dialog
            if (simpleDialogPreference != null) {
                val dialogFragment = SimpleDialogPreferenceCompat.newInstance(simpleDialogPreference.key)
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(parentFragmentManager, null)
            } else {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    class CreditsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.credits_preferences, rootKey)
        }
    }


    companion object {
        val TAG = SettingsActivity::class.java.simpleName
    }
}
