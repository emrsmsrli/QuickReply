package tr.edu.iyte.quickreply.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import tr.edu.iyte.quickreply.R;

public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }
}
