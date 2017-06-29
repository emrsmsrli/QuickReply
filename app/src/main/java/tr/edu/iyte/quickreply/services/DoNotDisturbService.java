package tr.edu.iyte.quickreply.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;

public class DoNotDisturbService extends Service {
    public static class DoNotDisturbListener extends BroadcastReceiver {
        private static final String TAG = "DoNotDisturbListener";

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                switch(notificationManager.getCurrentInterruptionFilter()) {
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        Log.i(TAG, "do not disturb enabled, starting call listener");
                        enableDND(context);
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        Log.i(TAG, "do not disturb disabled, stopping call listener");
                        disableDND(context);
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                    case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                    default:
                        // do nothing
                        break;
                }
            }
        }
    }

    public static final String EXTRA_REPLY
            = "tr.edu.iyte.quickreply.services.DoNotDisturbService.EXTRA_REPLY";
    private static final DoNotDisturbListener DO_NOT_DISTURB_LISTENER
            = new DoNotDisturbListener();

    private static String reply;
    private static boolean dndEnabled = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reply = intent.getStringExtra(EXTRA_REPLY);
        IntentFilter filter = new IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(DO_NOT_DISTURB_LISTENER, filter);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int interruptionFilter = notificationManager.getCurrentInterruptionFilter();
        if(interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS
            || interruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
            enableDND(this);
        } else {
            Toast.makeText(this, getString(R.string.dont_disturb_toast), Toast.LENGTH_SHORT).show();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(DO_NOT_DISTURB_LISTENER);
        reply = null;
        disableDND(this);
    }

    public static void enableDND(Context c) {
        QuickReplyTile.selectReply(reply);
        c.startService(new Intent(c, CallStopService.class));
        dndEnabled = true;
    }

    public static void disableDND(Context c) {
        if(!dndEnabled) return;

        c.stopService(new Intent(c, CallStopService.class));
        QuickReplyTile.selectReply("");
        QuickReplyTile.resetReplyCount();
        dndEnabled = false;
    }

    public static boolean isDndEnabled() {
        return dndEnabled;
    }
}
