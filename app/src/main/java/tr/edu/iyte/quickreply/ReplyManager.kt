package tr.edu.iyte.quickreply

import android.content.Context
import android.content.SharedPreferences
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import tr.edu.iyte.quickreply.helper.Constants

object ReplyManager : AnkoLogger {
    const val DEFAULT_REPLY = ""
    const val DEFAULT_REPLY_COUNT = 0

    private lateinit var prefs: SharedPreferences
    private lateinit var defaultReplies: Set<String>

    var currentReply: String
        get() = prefs.getString(Constants.SHARED_PREF_CURRENT_REPLY_KEY, DEFAULT_REPLY)
        set(value) {
            prefs.edit().putString(Constants.SHARED_PREF_CURRENT_REPLY_KEY, value).apply()
            info("Selected reply: $value")
        }
    var replyCount: Int
        get() = prefs.getInt(Constants.SHARED_PREF_REPLY_COUNT_KEY, DEFAULT_REPLY_COUNT)
        set(value) {
            prefs.edit().putInt(Constants.SHARED_PREF_REPLY_COUNT_KEY, value).apply()
            info("Reply count: $value")
        }
    val replies: Set<String>
        get() = prefs.getStringSet(Constants.SHARED_PREF_REPLIES_KEY, defaultReplies)

    fun hasNoReply() = replies.isEmpty()
    fun addReply(reply: String) {
        prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, replies + reply).apply()
        info("New reply added: $reply")
    }
    fun removeReply(reply: String) {
        prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, replies - reply).apply()
        info("Reply deleted: $reply")
    }

    fun resetToDefaultReplies(preserveCustom: Boolean = false) {
        var toReset = defaultReplies
        if(preserveCustom) {
            toReset = toReset.plus(replies)
        }
        prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, toReset).apply()
        info("Reset to default replies with preserving: $preserveCustom")
    }

    fun importReplies(replies: Set<String>) {
        prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, replies).apply()
        info("Replies imported: $replies")
    }

    fun deleteAllReplies() {
        prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, emptySet()).apply()
        info("All replies have been deleted")
    }

    fun init(context: Context) {
        prefs = context.getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        defaultReplies = setOf<String>(
                context.getString(R.string.default_reply_1),
                context.getString(R.string.default_reply_2),
                context.getString(R.string.default_reply_3))
    }
}