package tr.edu.iyte.quickreply.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.adapters.RuleAdapter;
import tr.edu.iyte.quickreply.helper.Rule;
import tr.edu.iyte.quickreply.helper.RuleManager;

public class PreferencesActivity
        extends Activity
        implements RuleManager.RuleReadListener {

    private static class DoNotDisturbListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                switch(notificationManager.getCurrentInterruptionFilter()) {
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        // TODO enable service, start selectreply with action extra
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
                        // TODO disable quickreply service, make sure to call quickreplytile.selectreply("");
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

    // TODO: 24/06/2017 implement alarm manager, rule and rule adapter
    private static final long ANIMATION_DURATION = 200;
    private static final int FAB_HEIGHT_IN_DIP = 72;
    private static final float ALPHA_FULL = 1f;
    private static final float ALPHA_HALF = .5f;
    private static final boolean SHOW = false;
    private static final boolean HIDE = true;

    private static final DoNotDisturbListener DO_NOT_DISTURB_LISTENER
            = new DoNotDisturbListener();

    private CheckBox enableRules;
    private SwitchCompat doNotDisturb;
    private View doNotDisturbLayout;
    private RecyclerView ruleList;
    private View noRulesView;
    private View mainLayout;
    private TextView noRulesTextView;
    private FloatingActionButton addRule;

    private String noRules;
    private String rulesDisabled;

    private RuleAdapter adapter;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        RuleManager.init(this);
        prefs = getSharedPreferences(QuickReplyTile.SHARED_PREF_KEY, MODE_PRIVATE);

        enableRules = (CheckBox) findViewById(R.id.auto_rules_enable);
        doNotDisturbLayout = findViewById(R.id.dnd_enable_layout);
        doNotDisturb = (SwitchCompat) doNotDisturbLayout.findViewById(R.id.do_not_dist_switch);
        mainLayout = findViewById(R.id.main_layout);
        ruleList = (RecyclerView) mainLayout.findViewById(R.id.rule_list);
        noRulesView = mainLayout.findViewById(R.id.no_rules);
        noRulesTextView = (TextView) noRulesView.findViewById(R.id.no_rules_text);
        addRule = (FloatingActionButton) findViewById(R.id.add_rule);

        noRules = getString(R.string.no_rules);
        rulesDisabled = getString(R.string.auto_rules_disabled);

        enableRules.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    doNotDisturbLayout.animate().alpha(ALPHA_FULL).setDuration(ANIMATION_DURATION).start();
                    doNotDisturb.setEnabled(true);

                    if(RuleManager.hasRules())
                        hideShowNoRule(HIDE, noRules);
                    else
                        hideShowNoRule(SHOW, noRules);

                    if(doNotDisturb.isChecked())
                        hideShowAddRule(HIDE);
                    else
                        hideShowAddRule(SHOW);
                } else {
                    doNotDisturbLayout.animate().alpha(ALPHA_HALF).setDuration(ANIMATION_DURATION).start();
                    doNotDisturb.setEnabled(false);
                    hideShowNoRule(SHOW, rulesDisabled);
                    hideShowAddRule(HIDE);
                }

                prefs.edit().putBoolean(QuickReplyTile.SHARED_PREF_RULE_ENABLE_KEY, isChecked).apply();
            }
        });

        enableRules.setChecked(prefs.getBoolean(QuickReplyTile.SHARED_PREF_RULE_ENABLE_KEY, false));

        adapter = new RuleAdapter();
        RuleManager.getRules(this);
        // TODO: 24/06/2017 implement loading while rules load
        ruleList.setLayoutManager(new LinearLayoutManager(this));
        ruleList.setAdapter(adapter);

        addRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 24/06/2017 add dummy rules
            }
        });

        doNotDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO get key from prefs
                // TODO: 24/06/2017 registerBroadcastReceiver
                // to listen dnd, alter select reply so that
                // if they select a reply, service shouldnt start immidiately
                /*IntentFilter filter = new IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
                registerReceiver(DO_NOT_DISTURB_LISTENER, filter);*/
                hideShowMainLayout(isChecked);
                hideShowAddRule(isChecked);
            }
        });
    }

    private void hideShowAddRule(boolean wasShown) {
        if(wasShown) { //hide
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FAB_HEIGHT_IN_DIP, r.getDisplayMetrics());
            addRule.animate()
                    .translationY(px)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setDuration(ANIMATION_DURATION)
                    .start();
        } else {
            addRule.animate()
                    .translationY(0)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setDuration(ANIMATION_DURATION)
                    .start();
        }
    }

    private void hideShowNoRule(boolean wasShown, String text) {
        noRulesTextView.setText(text);
        if(wasShown) { //hide
            noRulesView.animate()
                    .alpha(0)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            noRulesView.setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            noRulesView.animate()
                    .alpha(1)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            noRulesView.setVisibility(View.VISIBLE);
                        }
                    }).start();
        }
    }

    private void hideShowMainLayout(boolean wasShown) {
        if(wasShown) { //hide
            mainLayout.animate()
                    .scaleY(0)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mainLayout.setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            mainLayout.animate()
                    .scaleY(1)
                    .setDuration(ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            mainLayout.setVisibility(View.VISIBLE);
                        }
                    }).start();
        }
    }

    @Override
    public void onRulesRead(List<Rule> rules) {
        adapter.add(rules);
    }
}
