package tr.edu.iyte.quickreply.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.annotation.StringRes
import android.widget.ArrayAdapter
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.helper.SelectDiskPathDialog
import tr.edu.iyte.quickreply.helper.stopService
import tr.edu.iyte.quickreply.services.DNDService

class SettingsFragment : PreferenceFragment(),
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener,
        AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)

        val currentReply = defaultSharedPreferences
                .getString(getString(R.string.settings_reply_dnd_key), "")
        val f = findPreference(R.string.settings_listen_dnd_key) as CheckBoxPreference
        if(f.isChecked) {
            if (ReplyManager.hasNoReply()) {
                changeDNDReply("")
                f.isChecked = false
                stopListeningDND()
                toast(getString(R.string.dnd_has_no_reply))
            } else if (!currentReply.isEmpty() && !ReplyManager.replies.contains(currentReply)) {
                changeDNDReply(ReplyManager.replies.first())
                toast(getString(R.string.dnd_changed_reply))
            } else {
                findPreference(R.string.settings_reply_dnd_key).summary = currentReply
            }
        }

        manageListeners(this)
    }

    private fun findPreference(@StringRes prefKey: Int): Preference
            = super.findPreference(getString(prefKey))

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

        findPreference(R.string.settings_delete_rules_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_reply_dnd_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_reset_default_replies_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_import_export_rules_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_delete_all_replies_key).onPreferenceClickListener = listener

        findPreference(R.string.settings_listen_dnd_key).onPreferenceChangeListener = listener
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when(preference?.key) {
            getString(R.string.settings_delete_rules_key)          -> deleteRules()
            getString(R.string.settings_reply_dnd_key)             -> showDNDSelectReply()
            getString(R.string.settings_reset_default_replies_key) -> resetDefaultReplies()
            getString(R.string.settings_import_export_rules_key)   -> showImportExportDialog()
            getString(R.string.settings_delete_all_replies_key)    -> deleteReplies()
        }
        return true
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        when(preference?.key) {
            getString(R.string.settings_listen_dnd_key) -> {
                val listening = newValue as Boolean
                if(listening) {
                    if(ReplyManager.replies.isEmpty()) {
                        changeDNDReply("")
                        toast(getString(R.string.dnd_has_no_reply))
                        return false
                    } else {
                        startListeningDND()
                    }
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

    private fun deleteReplies() {
        AlertDialog.Builder(activity)
                .setMessage(R.string.settings_delete_replies_confirmation)
                .setPositiveButton(android.R.string.ok) { _, _ -> ReplyManager.deleteAllReplies() }
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun showDNDSelectReply() {
        val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1)
        adapter.addAll(ReplyManager.replies)
        AlertDialog.Builder(activity)
                .setTitle(R.string.settings_reply_dnd)
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setAdapter(adapter) { dialog, which ->
                    val reply = adapter.getItem(which)
                    changeDNDReply(reply)
                    dialog.dismiss()
                }
                .show()
    }

    private fun resetDefaultReplies() {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.settings_reset_default_replies))
                .setMessage(getString(R.string.settings_reset_default_replies_confirmation))
                .setNeutralButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { _, _ ->
                    ReplyManager.resetToDefaultReplies(preserveCustom = true) }
                .setNegativeButton(R.string.no) { _, _ -> ReplyManager.resetToDefaultReplies() }
                .show()
    }

    private fun startListeningDND() {
        var currentReply = defaultSharedPreferences.getString(
                getString(R.string.settings_reply_dnd_key), "")
        if(currentReply.isEmpty() ||
                !ReplyManager.replies.contains(currentReply)) {
            currentReply = ReplyManager.replies.first()
        }
        changeDNDReply(currentReply)
        startService<DNDService>()
        info("Started listening DND")
    }

    private fun stopListeningDND() {
        stopService<DNDService>()
        info("Stopped listening DND")
    }

    private fun changeDNDReply(reply: String) {
        defaultSharedPreferences.edit().putString(
                getString(R.string.settings_reply_dnd_key), reply).apply()
        findPreference(R.string.settings_reply_dnd_key).summary = reply
    }

    private fun showImportExportDialog() {
        val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1)
        adapter.addAll(*resources.getStringArray(R.array.settings_import_export)) // first export then import

        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.settings_import_export_rules))
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setAdapter(adapter) { dialog, which ->
                    when(which) {
                        0 -> exportRules() //export
                        1 -> importRules() //import
                    }
                    dialog.dismiss()
                }.show()
    }

    private fun exportRules() {
        SelectDiskPathDialog(object : SelectDiskPathDialog.OnPathSelectedListener {
            override fun onPathSelected(path: String) {
                toast("Selected $path")
                TODO("implement exporting")
            }
        }).show(activity)
    }

    private fun importRules() {
        SelectDiskPathDialog(object : SelectDiskPathDialog.OnPathSelectedListener {
            override fun onPathSelected(path: String) {
                toast("Selected $path")
                TODO("implement importing")
            }
        }).show(activity)
    }
}