package tr.edu.iyte.quickreply;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class IncomingCallListener extends PhoneStateListener {
    private static final String TAG = "IncomingCallListener";

    private Context c;

    public IncomingCallListener(Context c) {
        this.c = c;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        if(state == TelephonyManager.CALL_STATE_RINGING) {
            endCall(c);
        }
    }

    @SuppressWarnings("unchecked")
    private void endCall(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object telephonyService = m.invoke(tm); // Get the internal ITelephony object
            c = Class.forName(telephonyService.getClass().getName()); // Get its class
            m = c.getDeclaredMethod("endCall"); // Get the "endCall()" method
            m.setAccessible(true); // Make it accessible
            m.invoke(telephonyService); // invoke endCall()
        } catch(Exception e) {
            Log.e(TAG, "couldn't end call", e);
        }
    }
}
