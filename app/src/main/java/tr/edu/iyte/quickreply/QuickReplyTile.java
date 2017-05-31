package tr.edu.iyte.quickreply;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import tr.edu.iyte.quickreply.activities.RequestPermissionActivity;
import tr.edu.iyte.quickreply.activities.SelectReplyActivity;
import tr.edu.iyte.quickreply.services.CallStopService;

public class QuickReplyTile extends TileService {
    public static class IncomingCallListener extends PhoneStateListener {
        private static final String TAG = "IncomingCallListener";

        private final Context c;

        public IncomingCallListener(Context c) {
            this.c = c;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if(state == TelephonyManager.CALL_STATE_RINGING) {
                endCall(c, incomingNumber);
                sendReply(currentReply ,incomingNumber);
                prefs.edit().putInt(SHARED_PREF_REPLY_COUNT_KEY, ++replyCount).apply();
            }
        }

        @SuppressWarnings("unchecked")
        private void endCall(Context context, String phoneNum) {
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
                Log.i(TAG, "Call blocked from <" + phoneNum + ">");
            } catch(Exception e) {
                Log.e(TAG, "couldn't end call", e);
            }
        }

        private void sendReply(String reply, String phoneNum) {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNum, null, reply, null, null);
            Log.i(TAG, "SMS <" + reply + "> sent to <" + phoneNum + ">");
        }
    }

    private static final String TAG = "QuickReplyTile";
    private static final String SHARED_PREF_KEY = "tr.edu.iyte.quickreply.sharedprefs";
    private static final String SHARED_PREF_PERMISSIONS_KEY = "tr.edu.iyte.quickreply.sharedprefs.perms";
    private static final String SHARED_PREF_REPLY_COUNT_KEY = "tr.edu.iyte.quickreply.sharedprefs.replycount";
    private static final String SHARED_PREF_REPLIES_KEY = "tr.edu.iyte.quickreply.sharedprefs.replies";

    private static final String DEFAULT_TITLE = "Quick Reply";
    private static final int DEFAULT_REPLY_COUNT = 0;
    private static final boolean DEFAULT_HAS_PERMISSIONS = false;
    private static final String DEFAULT_REPLY = "";
    private static final Set<String> DEFAULT_REPLIES = new HashSet<>();

    private static boolean hasPermissions = DEFAULT_HAS_PERMISSIONS;
    private static int replyCount = DEFAULT_REPLY_COUNT;
    private static Set<String> replies = DEFAULT_REPLIES;
    private static SharedPreferences prefs = null;
    private static String currentReply = DEFAULT_REPLY;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        if(DEFAULT_REPLIES.isEmpty()) {
            DEFAULT_REPLIES.add(getResources().getString(R.string.default_reply_1));
            DEFAULT_REPLIES.add(getResources().getString(R.string.default_reply_2));
            DEFAULT_REPLIES.add(getResources().getString(R.string.default_reply_3));
        }
        Log.d(TAG, "Tile added");
    }

    @Override
    public void onStartListening() {
        Log.i(TAG, "Started listening");
        if(prefs == null)
            prefs = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE);
        hasPermissions = prefs.getBoolean(SHARED_PREF_PERMISSIONS_KEY, DEFAULT_HAS_PERMISSIONS);
        replyCount = prefs.getInt(SHARED_PREF_REPLY_COUNT_KEY, DEFAULT_REPLY_COUNT);
        replies = prefs.getStringSet(SHARED_PREF_REPLIES_KEY, DEFAULT_REPLIES);

        updateTile();
    }

    @Override
    public void onStopListening() {
        Log.d(TAG, "Stopped listening");
    }

    @Override
    public void onTileRemoved() {
        Log.d(TAG, "Tile removed");
        replyCount = DEFAULT_REPLY_COUNT;
    }

    @Override
    public void onClick() {
        super.onClick();
        if(!hasPermissions) {
            startActivityAndCollapse(new Intent(this, RequestPermissionActivity.class));
            return;
        }

        if(!currentReply.isEmpty()) {
            reset();
            return;
        }

        startActivityAndCollapse(new Intent(this, SelectReplyActivity.class));
    }

    private void updateTile() {
        if(currentReply.isEmpty()) {
            defaultTile();
            return;
        }

        Tile t = getQsTile();
        t.setState(Tile.STATE_ACTIVE);
        t.setLabel(String.format(Locale.getDefault(), "(%d) %s", replyCount, currentReply));
        t.setIcon(Icon.createWithResource(this, R.drawable.ic_chat_bubble_black_24dp));
        t.updateTile();
    }

    private void defaultTile() {
        Tile t = getQsTile();
        t.setState(Tile.STATE_INACTIVE);
        t.setLabel(DEFAULT_TITLE);
        t.setIcon(Icon.createWithResource(this, R.drawable.ic_chat_bubble_outline_black_24dp));
        t.updateTile();
    }

    private void reset() {
        stopService(new Intent(this, CallStopService.class));
        defaultTile();
        currentReply = DEFAULT_REPLY;
        prefs.edit().putInt(SHARED_PREF_REPLY_COUNT_KEY, DEFAULT_REPLY_COUNT).apply();
        Log.i(TAG, "Reset");
    }

    public static void permissionsGranted() {
        hasPermissions = true;
        prefs.edit().putBoolean(SHARED_PREF_PERMISSIONS_KEY, hasPermissions).apply();
    }

    public static Set<String> getReplies() {
        return replies;
    }

    public static void selectReply(String reply) {
        currentReply = reply;
        Log.i(TAG, "Selected reply: " + reply);
    }

    public static boolean addReply(String reply) {
        replies.add(reply);
        prefs.edit().putStringSet(SHARED_PREF_REPLIES_KEY, replies).apply();
        Log.i(TAG, "Added new reply: " + reply);
        return replies.size() == 1;
    }

    public static void removeReply(String reply) {
        replies.remove(reply);
        prefs.edit().putStringSet(SHARED_PREF_REPLIES_KEY, replies).apply();
        Log.i(TAG, "Reply removed: " + reply);
    }

    public static boolean hasNoReply() {
        return replies.isEmpty();
    }
}
