package tr.edu.iyte.quickreply.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.ArrayAdapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.verbose

import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.fragments.SettingsFragment

class SettingsActivity : Activity() {
    private val FRAGMENT_TAG = "settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment(), FRAGMENT_TAG)
                .commit()
    }
}
