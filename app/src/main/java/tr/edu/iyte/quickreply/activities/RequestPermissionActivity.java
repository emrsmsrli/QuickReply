package tr.edu.iyte.quickreply.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import tr.edu.iyte.quickreply.QuickReplyTile;

public class RequestPermissionActivity extends Activity {
    private static final String TAG = "RequestPermissionAct";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
        requestPermissions(new String[] {Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1) {
            boolean shouldCheck = true;
            for(int i = 0; i < permissions.length; ++i) {
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Permission " + permissions[i] + " denied");
                    shouldCheck = false;
                } else
                    Log.i(TAG, "Permission " + permissions[i] + " granted");
            }

            if(shouldCheck) {
                QuickReplyTile.permissionsGranted();
                startActivity(new Intent(this, SelectReplyActivity.class));
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        finish();
    }
}
