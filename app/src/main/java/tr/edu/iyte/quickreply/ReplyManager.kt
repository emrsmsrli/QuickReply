package tr.edu.iyte.quickreply

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import tr.edu.iyte.quickreply.helper.Constants

object ReplyManager {
    private const val TAG = "ReplyManager"
    private const val DEFAULT_REPLY = ""
    private const val DEFAULT_REPLY_COUNT = 0

    private lateinit var prefs: SharedPreferences
    private lateinit var defaultReplies: Set<String>

    var currentReply: String
        get() = prefs.getString(Constants.SHARED_PREF_CURRENT_REPLY_KEY, DEFAULT_REPLY)
        set(value) { prefs.edit().putString(Constants.SHARED_PREF_CURRENT_REPLY_KEY, value).apply(); Log.i(TAG, "Selected reply: $value") }
    var replyCount: Int
        get() = prefs.getInt(Constants.SHARED_PREF_REPLY_COUNT_KEY, DEFAULT_REPLY_COUNT)
        set(value) = prefs.edit().putInt(Constants.SHARED_PREF_REPLY_COUNT_KEY, value).apply()
    val replies: Set<String>
        get() = prefs.getStringSet(Constants.SHARED_PREF_REPLIES_KEY, defaultReplies)

    fun addReply(reply: String) = prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, replies.plus(reply)).apply()
    fun removeReply(reply: String) = prefs.edit().putStringSet(Constants.SHARED_PREF_REPLIES_KEY, replies.minus(reply)).apply()
    fun hasNoReply() = replies.isEmpty()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE)
        defaultReplies = setOf<String>(
                context.getString(R.string.default_reply_1),
                context.getString(R.string.default_reply_2),
                context.getString(R.string.default_reply_3))
    }
}