package tr.edu.iyte.quickreply;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


public class CallStopService extends Service {
    private final PhoneStateListener CALL_LISTENER = new IncomingCallListener(this);

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(CALL_LISTENER, PhoneStateListener.LISTEN_CALL_STATE);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(CALL_LISTENER, PhoneStateListener.LISTEN_NONE);
        super.onDestroy();
    }
}
