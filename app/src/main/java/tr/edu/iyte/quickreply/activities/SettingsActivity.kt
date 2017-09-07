package tr.edu.iyte.quickreply.activities

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_settings.*
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.fragments.SettingsFragment

class SettingsActivity : Activity() {
    private val FRAGMENT_TAG = "settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        back_button.setOnClickListener {
            onBackPressed()
        }

        fragmentManager.beginTransaction()
                .replace(R.id.settings_fragment, SettingsFragment(), FRAGMENT_TAG)
                .commit()
    }
}
