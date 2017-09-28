package tr.edu.iyte.quickreply.fragments

import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.widget.ArrayAdapter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.helper.SelectFileDialog
import tr.edu.iyte.quickreply.helper.stopService
import tr.edu.iyte.quickreply.services.DNDService
import java.io.*

class SettingsFragment : PreferenceFragment(),
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener,
        AnkoLogger {
    private val ruleExportFile = "quick-reply-rule-export"
    private val replyExportFile = "quick-reply-reply-export"

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
    }

    override fun onResume() {
        super.onResume()
        setListeners(this)
    }

    private fun findPreference(@StringRes prefKey: Int): Preference
            = super.findPreference(getString(prefKey))

    override fun onStop() {
        setListeners(null)
        super.onStop()
    }

    private fun setListeners(listener: SettingsFragment?) {
        if(listener == null) {
            verbose("Resetting listeners")
        } else {
            verbose("Setting listeners")
        }

        findPreference(R.string.settings_delete_rules_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_reply_dnd_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_reset_default_replies_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_import_export_rules_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_import_export_replies_key).onPreferenceClickListener = listener
        findPreference(R.string.settings_delete_all_replies_key).onPreferenceClickListener = listener

        findPreference(R.string.settings_listen_dnd_key).onPreferenceChangeListener = listener
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        when(preference?.key) {
            getString(R.string.settings_delete_rules_key)          -> deleteRules()
            getString(R.string.settings_reply_dnd_key)             -> showDNDSelectReply()
            getString(R.string.settings_reset_default_replies_key) -> resetDefaultReplies()
            getString(R.string.settings_import_export_rules_key)   -> showImportExportDialog(isForRules = true)
            getString(R.string.settings_import_export_replies_key) -> showImportExportDialog(isForRules = false)
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
        alert(message = R.string.settings_delete_replies_confirmation,
                title = R.string.settings_delete_all_replies) {
            okButton { ReplyManager.deleteAllReplies() }
            cancelButton { it.dismiss() }
        }.show()
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
        alert(message = R.string.settings_reset_default_replies_confirmation,
                title = R.string.settings_reset_default_replies) {
            neutralPressed(android.R.string.cancel) { it.dismiss() }
            positiveButton(R.string.yes) { ReplyManager.resetToDefaultReplies(preserveCustom = true) }
            negativeButton(R.string.no) { ReplyManager.resetToDefaultReplies(preserveCustom = false) }
        }.show()
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

    private fun showImportExportDialog(isForRules: Boolean) {
        val adapter = ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1)
        adapter.addAll(*resources.getStringArray(R.array.settings_import_export)) // first export then import

        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.settings_import_export))
                .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setAdapter(adapter) { dialog, which ->
                    val isImportMode = which == 1
                    SelectFileDialog(fileSelectMode = isImportMode) {
                        when(which) {
                            0 -> if(isForRules) exportRules(it) else exportReplies(it)  //export
                            1 -> if(isForRules) importRules(it) else importReplies(it)  //import
                        }
                        dialog.dismiss()
                    }.show(activity)
                }.show()
    }

    private fun exportRules(path: String) {
        toast("Selected $path")
        TODO("implement exporting")
    }

    private fun importRules(path: String) {
        toast("Selected $path")
        TODO("implement importing")
    }

    private fun exportReplies(path: String) {
        doAsync {
            val repliesJsonByteArray = Gson().toJson(ReplyManager.replies).toByteArray()
            DataOutputStream(FileOutputStream(File(path, replyExportFile)))
                    .use { it.write(repliesJsonByteArray) }
            fragmentUiThread {
                toast("${getString(R.string.reply_exported)} $path")
            }
        }
    }

    private fun importReplies(path: String) {
        doAsync {
            fun notValid() {
                fragmentUiThread {
                    toast("${getString(R.string.file_not_valid)} $path")
                }
            }

            val repliesJsonByteArray = String(File(path).readBytes())
            if(repliesJsonByteArray.isEmpty()) {
                notValid()
                return@doAsync
            }

            try {
                val repliesJson = Gson()
                        .fromJson(repliesJsonByteArray, HashSet<String>().javaClass)
                ReplyManager.importReplies(repliesJson)
                fragmentUiThread {
                    toast("${getString(R.string.reply_imported)} $path")
                }
            } catch(e: JsonSyntaxException) {
                notValid()
            }
        }
    }
}