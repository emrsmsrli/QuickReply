package tr.edu.iyte.quickreply.services

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.R
import tr.edu.iyte.quickreply.ReplyManager
import tr.edu.iyte.quickreply.helper.stopService

class DNDService : Service(), AnkoLogger {
    private val DNDListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED == intent?.action) {
                when(notificationManager.currentInterruptionFilter) {
                    NotificationManager.INTERRUPTION_FILTER_ALARMS,
                    NotificationManager.INTERRUPTION_FILTER_NONE -> enableDND()
                    NotificationManager.INTERRUPTION_FILTER_ALL  -> disableDND()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
        registerReceiver(DNDListener, filter)

        val currentFilter = notificationManager.currentInterruptionFilter
        if(currentFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS
                || currentFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
            enableDND()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        unregisterReceiver(DNDListener)
        disableDND()
        super.onDestroy()
    }

    fun enableDND() {
        info("dnd enabled, starting quickreply")
        isDNDEnabled = true
        ReplyManager.currentReply =
                defaultSharedPreferences.getString(getString(R.string.settings_reply_dnd_key),
                        ReplyManager.replies.first())
        startService<CallStopService>()
    }

    fun disableDND() {
        info("dnd disabled, stopping quickreply")
        if(!isDNDEnabled) return

        isDNDEnabled = false
        stopService<CallStopService>()
        ReplyManager.currentReply = ReplyManager.DEFAULT_REPLY
        ReplyManager.replyCount = ReplyManager.DEFAULT_REPLY_COUNT
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        var isDNDEnabled = false
    }
}