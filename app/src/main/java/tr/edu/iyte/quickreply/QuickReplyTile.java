package tr.edu.iyte.quickreply;

import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.TileService;
import android.util.Log;

public class QuickReplyTile extends TileService {
    private static final String TAG = "QuickReplyTile";
    private static final String SHARED_PREF_KEY = "tr.edu.iyte.quickreply.sharedprefs";
    private static final String SHARED_PREF_PERMISSIONS_KEY = "tr.edu.iyte.quickreply.sharedprefs.perms";

    private boolean isListening = false;
    private static boolean hasPermissions = false;
    private static SharedPreferences prefs;

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.i(TAG, "Tile added");
    }

    @Override
    public void onStartListening() {
        Log.i(TAG, "Started listening");
        prefs = getSharedPreferences(SHARED_PREF_KEY, MODE_PRIVATE);
        hasPermissions = prefs.getBoolean(SHARED_PREF_PERMISSIONS_KEY, false);
        if(!hasPermissions)
            startActivity(new Intent(this, RequestPermissionActivity.class));
    }

    @Override
    public void onStopListening() {
        Log.i(TAG, "Stopped listening");
    }

    @Override
    public void onTileRemoved() {
        Log.i(TAG, "Tile removed");
    }

    @Override
    public void onClick() {
        super.onClick();
        if(!hasPermissions)
            return;

        updateTile();
    }

    private void updateTile() {

    }

    public static void permissionsGranted() {
        hasPermissions = true;
        prefs.edit()
                .putBoolean(SHARED_PREF_PERMISSIONS_KEY, hasPermissions)
                .apply();
    }
}
