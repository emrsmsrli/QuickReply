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

class SettingsActivity : Activity() {
    private val FRAGMENT_TAG = "settings"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment(), FRAGMENT_TAG)
                .commit()
    }

    companion object {
        class SettingsFragment : PreferenceFragment(),
                Preference.OnPreferenceClickListener,
                Preference.OnPreferenceChangeListener,
                AnkoLogger {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                addPreferencesFromResource(R.xml.preferences)
                manageListeners(this)
            }

            override fun onDestroy() {
                manageListeners(null)
                super.onDestroy()
            }

            private fun manageListeners(listener: SettingsFragment?) {
                if(listener == null) {
                    verbose("Resetting listeners")
                } else {
                    verbose("Setting listeners")
                }

                findPreference(getString(R.string.settings_delete_rules_key)).onPreferenceClickListener = listener
                findPreference(getString(R.string.settings_reply_dnd_key)).onPreferenceClickListener = listener
                findPreference(getString(R.string.settings_reset_default_replies_key)).onPreferenceClickListener = listener
                findPreference(getString(R.string.settings_import_export_rules_key)).onPreferenceClickListener = listener

                findPreference(getString(R.string.settings_listen_dnd_key)).onPreferenceChangeListener = listener
            }

            override fun onPreferenceClick(preference: Preference?): Boolean {
                when(preference?.key) {
                    getString(R.string.settings_delete_rules_key)          -> deleteRules()
                    getString(R.string.settings_reply_dnd_key)             -> selectDNDReply()
                    getString(R.string.settings_reset_default_replies_key) -> resetDefaultReplies()
                    getString(R.string.settings_import_export_rules_key)   -> showImportExportDialog()
                }
                return true
            }

            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                when(preference?.key) {
                    getString(R.string.settings_listen_dnd_key) -> {
                        val listening = newValue as Boolean
                        if(listening) {
                            startListeningDND()
                            TODO("manage select reply for dnd")
                        } else {
                            stopListeningDND()
                        }
                    }
                }
                return true
            }

            private fun deleteRules() {
                info("Deleting all rules")
                TODO()
            }

            private fun selectDNDReply() {
                TODO()
            }

            private fun resetDefaultReplies() {
                AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.settings_reset_default_replies))
                        .setMessage(getString(R.string.settings_reset_default_replies_confirmation))
                        .setPositiveButton(android.R.string.yes) { _, _ -> ReplyManager.resetToDefaultReplies() }
                        .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.dismiss() }
                        .show()
            }

            private fun startListeningDND() {
                info("Started listening DND")
            }

            private fun stopListeningDND() {
                info("Stopped listening DND")
            }

            private fun showImportExportDialog() {
                val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1)
                adapter.addAll(*resources.getStringArray(R.array.settings_import_export)) // first export then import

                val dialogBuilder = AlertDialog.Builder(activity)
                dialogBuilder
                        .setTitle(getString(R.string.settings_import_export_rules))
                        .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                        .setAdapter(adapter) { dialog, which ->
                            when(which) {
                                0 -> exportRules() //export
                                1 -> importRules() //import
                            }
                            dialog.dismiss()
                        }
                        .show()
            }

            private fun exportRules() {
                TODO()
            }

            private fun importRules() {
                TODO()
            }
        }
    }
}
