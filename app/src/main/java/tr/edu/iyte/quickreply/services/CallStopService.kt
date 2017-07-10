package tr.edu.iyte.quickreply.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import tr.edu.iyte.quickreply.QuickReplyTile

class CallStopService : Service() {
    private val TAG = "CallStopService"
    private val CALL_LISTENER = QuickReplyTile.Companion.IncomingCallListener(this)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(CALL_LISTENER, PhoneStateListener.LISTEN_CALL_STATE)
        Log.i(TAG, "Call listen service started")
        return Service.START_STICKY
    }

    override fun onDestroy() {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(CALL_LISTENER, PhoneStateListener.LISTEN_NONE)
        Log.i(TAG, "Call listen service stopped")
        super.onDestroy()
    }
}