package tr.edu.iyte.quickreply.services;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

import tr.edu.iyte.quickreply.rules.Rule;
import tr.edu.iyte.quickreply.rules.RuleManager;


public class AlarmService extends Service {
    public class AlarmServiceBinder extends Binder {
        public AlarmService getService() {
            return AlarmService.this;
        }
    }

    private AlarmManager manager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(!DoNotDisturbService.isDndEnabled()) {
            RuleManager.getRules(new RuleManager.RuleReadListener() {
                @Override
                public void onRulesRead(List<Rule> rules) {
                    for(Rule rule : rules)
                        ;//enable rules
                }
            });
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        RuleManager.getRules(new RuleManager.RuleReadListener() {
            @Override
            public void onRulesRead(List<Rule> rules) {
                for(Rule rule : rules)
                    ;//disable rules
            }
        });

        super.onDestroy();
    }

    public void enableRule(Rule rule) {

    }

    public void disableRule(Rule rule) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AlarmServiceBinder();
    }
}
