package tr.edu.iyte.quickreply.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import tr.edu.iyte.quickreply.QuickReplyTile;

public class CallStopService extends Service {
    private static final String TAG = "CallStopService";
    private final PhoneStateListener CALL_LISTENER = new QuickReplyTile.IncomingCallListener(this);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(CALL_LISTENER, PhoneStateListener.LISTEN_CALL_STATE);
        Log.i(TAG, "Call listen service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(CALL_LISTENER, PhoneStateListener.LISTEN_NONE);
        Log.i(TAG, "Call listen service stopped");
        super.onDestroy();
    }
}
