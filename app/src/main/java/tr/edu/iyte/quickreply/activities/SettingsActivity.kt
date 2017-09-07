package tr.edu.iyte.quickreply.activities

import android.app.Activity
import android.os.Bundle
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
