package tr.edu.iyte.quickreply

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import tr.edu.iyte.quickreply.activities.SelectReplyActivity

class QuickReplyTile : TileService() {
    private val TAG = "QuickReplyTile"

    override fun onBind(intent: Intent): IBinder? {
        ReplyManager.init(this)
        Log.v(TAG, "Bound")
        return super.onBind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.v(TAG, "Unbound")
        return super.onUnbind(intent)
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.i(TAG, "Started listening")

        if(ReplyManager.currentReply.isEmpty())
            updateTile()
        else
            updateTile(state = Tile.STATE_ACTIVE,
                    label = "(${ReplyManager.replyCount}) ${ReplyManager.currentReply}",
                    icon = R.drawable.ic_chat_bubble_black_24dp)
    }

    override fun onStopListening() {
        Log.i(TAG, "Stopped listening")
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        Log.i(TAG, "Clicked")

        /*if(!hasPermissions()) {
            startActivityAndCollapse(Intent(this, RequestPermissionActivity::class.java))
            return
        }*/

        if (!ReplyManager.currentReply.isEmpty()) {
            //reset()
            return
        }

        //startActivityAndCollapse(Intent(this, SelectReplyActivity::class.java))
    }

    fun updateTile(state: Int = Tile.STATE_INACTIVE,
                   label: String = "Quick Reply",
                   icon: Int = R.drawable.ic_chat_bubble_outline_black_24dp) {
        qsTile.state = state
        qsTile.label = label
        qsTile.icon = Icon.createWithResource(this, icon)
        qsTile.updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Log.i(TAG, "Tile added")
    }

    override fun onTileRemoved() {
        Log.i(TAG, "Tile removed")
        super.onTileRemoved()
    }

    companion object {
        class IncomingCallListener(val c: Context) : PhoneStateListener() {
            private val TAG = "IncomingCallListener"

            override fun onCallStateChanged(state: Int, incomingNumber: String?) {
                super.onCallStateChanged(state, incomingNumber)
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    endCall(c, incomingNumber!!)
                    /*sendReply(currentReply, incomingNumber)
                    prefs.edit().putInt(SHARED_PREF_REPLY_COUNT_KEY, ++replyCount).apply()*/
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
                    Log.i(TAG, "Call blocked from <$phoneNum>")
                } catch (e: Exception) {
                    Log.e(TAG, "couldn't end call", e)
                }

            }

            private fun sendReply(reply: String, phoneNum: String) {
                val sms = SmsManager.getDefault()
                sms.sendTextMessage(phoneNum, null, reply, null, null)
                Log.i(TAG, "SMS <$reply> sent to <$phoneNum>")
            }
        }
    }
}