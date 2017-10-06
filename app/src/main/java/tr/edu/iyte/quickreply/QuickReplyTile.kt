package tr.edu.iyte.quickreply

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import org.jetbrains.anko.*
import tr.edu.iyte.quickreply.activities.ReplyRuleActivity
import tr.edu.iyte.quickreply.helper.database
import tr.edu.iyte.quickreply.helper.startActivityAndCollapse
import tr.edu.iyte.quickreply.helper.stopService
import tr.edu.iyte.quickreply.services.CallStopService

class QuickReplyTile : TileService(), AnkoLogger {
    override fun onBind(intent: Intent): IBinder? {
        ReplyManager.init(this)
        verbose("Bound")
        return super.onBind(intent)
    }

    override fun onStartListening() {
        super.onStartListening()
        info("Started listening")

        if(ReplyManager.currentReply.isEmpty())
            updateTile()
        else
            updateTile(state = Tile.STATE_ACTIVE,
                    label = "(${ReplyManager.replyCount}) ${ReplyManager.currentReply}",
                    icon = R.drawable.ic_chat_bubble_black_24dp)
    }

    override fun onStopListening() {
        info("Stopped listening")
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        info("Clicked")

        if (!ReplyManager.currentReply.isEmpty()) {
            reset()
            return
        }

        startActivityAndCollapse<ReplyRuleActivity>()
    }

    private fun updateTile(state: Int = Tile.STATE_INACTIVE,
                   label: String = "Quick Reply",
                   icon: Int = R.drawable.ic_chat_bubble_outline_black_24dp) {
        qsTile?.state = state
        qsTile?.label = label
        qsTile?.icon = Icon.createWithResource(this, icon)
        info("Updating tile with label: $label")
        qsTile?.updateTile()
    }

    private fun reset() {
        info("Resetting tile service")
        stopService<CallStopService>()
        updateTile()
        ReplyManager.replyCount = ReplyManager.DEFAULT_REPLY_COUNT
        ReplyManager.currentReply = ReplyManager.DEFAULT_REPLY
    }

    override fun onTileAdded() {
        super.onTileAdded()
        info("Tile added")
    }

    override fun onTileRemoved() {
        info("Tile removed")
        super.onTileRemoved()
    }

    companion object {
        internal const val SMS_DELIV_INTENT_REQUEST_CODE = 2 shl 6
        internal const val EXTRA_CALLER = "caller"
        internal const val EXTRA_TYPE = "type"
        internal const val EXTRA_SENT_ON = "sentOn"
        internal const val EXTRA_TYPE_PHONE = 0
        //internal const val EXTRA_TYPE_SMS = 1

        class SMSSentReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val isDelivered = resultCode == Activity.RESULT_OK

                context?.database?.addToHistory(
                        ReplyLog(callerNumber = intent!!.getStringExtra(EXTRA_CALLER),
                                reply = ReplyManager.currentReply,
                                type = if(intent.getIntExtra(EXTRA_TYPE, EXTRA_TYPE_PHONE) == EXTRA_TYPE_PHONE)
                                    ReplyLogType.CALL else ReplyLogType.SMS,
                                sentOn = intent.getLongExtra(EXTRA_SENT_ON, System.currentTimeMillis()),
                                isDelivered = isDelivered))
            }
        }

        class IncomingCallListener(val context: Context) : PhoneStateListener(), AnkoLogger {
            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                super.onCallStateChanged(state, incomingNumber)
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    endCall(context, incomingNumber!!)
                    sendReply(ReplyManager.currentReply, incomingNumber)
                    ReplyManager.replyCount++
                }
            }

            private fun endCall(context: Context, phoneNum: String) {
                try {
                    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    var c = Class.forName(tm.javaClass.name)
                    var m = c.getDeclaredMethod("getITelephony")
                    m.isAccessible = true
                    val telephonyService = m.invoke(tm) // Get the internal ITelephony object
                    c = Class.forName(telephonyService.javaClass.name) // Get its class
                    m = c.getDeclaredMethod("endCall") // Get the "endCall()" method
                    m.isAccessible = true // Make it accessible
                    m.invoke(telephonyService) // invoke endCall()
                    info("Call blocked from <$phoneNum>")
                } catch (e: Exception) {
                    error("Couldn't end call", e)
                }

            }

            private fun sendReply(reply: String, phoneNum: String) {
                val sms = SmsManager.getDefault()
                val receiverIntent = context.intentFor<SMSSentReceiver>(EXTRA_CALLER to phoneNum,
                        EXTRA_TYPE to EXTRA_TYPE_PHONE,
                        EXTRA_SENT_ON to System.currentTimeMillis())
                val sentIntent = PendingIntent.getBroadcast(context,
                        SMS_DELIV_INTENT_REQUEST_CODE,
                        receiverIntent,
                        PendingIntent.FLAG_ONE_SHOT)
                sms.sendTextMessage(phoneNum, null, reply, sentIntent, null)
                info("SMS <$reply> sent to <$phoneNum>")
            }
        }

        // TODO implement incoming sms listener
    }
}