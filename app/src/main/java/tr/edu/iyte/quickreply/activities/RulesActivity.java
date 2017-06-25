package tr.edu.iyte.quickreply.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.adapters.RuleAdapter;
import tr.edu.iyte.quickreply.helper.Rule;
import tr.edu.iyte.quickreply.helper.RuleManager;
import tr.edu.iyte.quickreply.services.CallStopService;

public class RulesActivity
        extends Activity
        implements RuleManager.RuleReadListener,
        PopupMenu.OnMenuItemClickListener {

    private static class DoNotDisturbListener extends BroadcastReceiver {
        public static boolean dndEnabled = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                switch(notificationManager.getCurrentInterruptionFilter()) {
                    case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                    case NotificationManager.INTERRUPTION_FILTER_ALL:
                        enableDND(context);
                        break;
                    case NotificationManager.INTERRUPTION_FILTER_NONE:
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

        public static void enableDND(Context c) {
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
    }

    // TODO: 24/06/2017 implement alarm manager, rule and rule adapter
    private static final long ANIMATION_DURATION = 200;
    private static final int FAB_HEIGHT_IN_DIP = 72;
    private static final float ALPHA_FULL = 1f;
    private static final float ALPHA_HALF = .5f;
    private static final float ALPHA_NONE = 0;
    private static final boolean SHOW = false;
    private static final boolean HIDE = true;

    private static final DoNotDisturbListener DO_NOT_DISTURB_LISTENER
            = new DoNotDisturbListener();

    private SwitchCompat doNotDisturb;
    private View doNotDisturbLayout;
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
        setContentView(R.layout.activity_rules);

        RuleManager.init(this);
        prefs = getSharedPreferences(QuickReplyTile.SHARED_PREF_KEY, MODE_PRIVATE);

        final CheckBox enableRules = (CheckBox) findViewById(R.id.auto_rules_enable);
        final View more = findViewById(R.id.more);
        doNotDisturbLayout = findViewById(R.id.dnd_enable_layout);
        doNotDisturb = (SwitchCompat) doNotDisturbLayout.findViewById(R.id.do_not_dist_switch);
        mainLayout = findViewById(R.id.main_layout);
        final RecyclerView ruleList = (RecyclerView) mainLayout.findViewById(R.id.rule_list);
        noRulesView = mainLayout.findViewById(R.id.no_rules);
        noRulesTextView = (TextView) noRulesView.findViewById(R.id.no_rules_text);
        addRule = (FloatingActionButton) findViewById(R.id.add_rule);

        noRules = getString(R.string.no_rules);
        rulesDisabled = getString(R.string.auto_rules_disabled);

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(RulesActivity.this, v);
                popupMenu.setOnMenuItemClickListener(RulesActivity.this);
                popupMenu.inflate(R.menu.pref_menu);
                popupMenu.show();
            }
        });

        enableRules.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    doNotDisturbLayout.animate().alpha(ALPHA_FULL).setDuration(ANIMATION_DURATION).start();
                    doNotDisturb.setEnabled(true);

                    toggleNoRule(RuleManager.hasRules() ? HIDE : SHOW, noRules);
                    toggleAddRule(doNotDisturb.isChecked() ? HIDE : SHOW);

                    // TODO: 25/06/2017 check rules?
                    if(doNotDisturb.isChecked()) {
                        enableDoNotDisturb();
                    }
                } else {
                    doNotDisturbLayout.animate().alpha(ALPHA_HALF).setDuration(ANIMATION_DURATION).start();
                    doNotDisturb.setEnabled(false);

                    toggleNoRule(SHOW, rulesDisabled);
                    toggleAddRule(HIDE);

                    // TODO: 25/06/2017 check rules?
                    if(doNotDisturb.isChecked()) {
                        disableDoNotDisturb();
                    }
                }

                prefs.edit().putBoolean(QuickReplyTile.SHARED_PREF_RULE_ENABLE_KEY, isChecked).apply();
            }
        });

        doNotDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)   enableDoNotDisturb();
                else            disableDoNotDisturb();

                toggleMainLayout(isChecked);
                toggleAddRule(isChecked);
                prefs.edit().putBoolean(QuickReplyTile.SHARED_PREF_RULE_DND_ENABLE_KEY, isChecked).apply();
            }
        });

        enableRules.setChecked(prefs.getBoolean(QuickReplyTile.SHARED_PREF_RULE_ENABLE_KEY, false));
        doNotDisturb.setChecked(prefs.getBoolean(QuickReplyTile.SHARED_PREF_RULE_DND_ENABLE_KEY, false));

        adapter = new RuleAdapter();
        RuleManager.getRules(this);
        ruleList.setLayoutManager(new LinearLayoutManager(this));
        ruleList.setAdapter(adapter);

        addRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNoRule(HIDE, null);
                // TODO implement actual adding with starting selectReplyActivity, with extra code
                adapter.removeAll();
                List<String> days = new ArrayList<>();
                days.add("Monday");
                days.add("Tuesday");
                days.add("Wednesday");
                days.add("Thursday");
                adapter.add(new Rule(UUID.randomUUID().toString(), "", System.currentTimeMillis(), System.currentTimeMillis(), days));
                days.add("Weekends");
                adapter.add(new Rule(UUID.randomUUID().toString(), "", System.currentTimeMillis(), System.currentTimeMillis(), days));
                days.add("Friday");
                adapter.add(new Rule(UUID.randomUUID().toString(), "", System.currentTimeMillis(), System.currentTimeMillis(), days));
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.remove_all:
                toggleNoRule(SHOW, noRules);
                adapter.removeAll();
                break;
            default:
                return false;
        }
        return true;
    }

    private void enableDoNotDisturb() {
        IntentFilter filter = new IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED);
        registerReceiver(DO_NOT_DISTURB_LISTENER, filter);
        Toast.makeText(this, getString(R.string.dont_disturb_toast), Toast.LENGTH_SHORT).show();

        // TODO: 25/06/2017 disable all rules, but save them somewhere first
    }

    private void disableDoNotDisturb() {
        unregisterReceiver(DO_NOT_DISTURB_LISTENER);
        DoNotDisturbListener.disableDND(this);

        // TODO: 25/06/2017 restore enable states
    }

    private void toggleAddRule(boolean wasShown) {
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

    private void toggleNoRule(boolean wasShown, String text) {
        if(wasShown) { //hide
            noRulesView.animate()
                    .alpha(ALPHA_NONE)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            noRulesView.setVisibility(View.GONE);
                        }
                    }).start();
        } else {
            noRulesTextView.setText(text);
            noRulesView.animate()
                    .alpha(ALPHA_FULL)
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

    private void toggleMainLayout(boolean wasShown) {
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
        if(rules.isEmpty())
            return;

        toggleNoRule(HIDE, null);
        adapter.addAll(rules);
    }
}
