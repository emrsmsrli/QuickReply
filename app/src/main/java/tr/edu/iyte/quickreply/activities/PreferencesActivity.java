package tr.edu.iyte.quickreply.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
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

import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.adapters.RuleAdapter;

public class PreferencesActivity extends Activity {

    // TODO: 24/06/2017 implement alarm manager, rule and rule adapter
    private static final long ANIMATION_DURATION = 200;
    private static final float ALPHA_FULL = 1f;
    private static final float ALPHA_HALF = .5f;

    private CheckBox enableRules;
    private SwitchCompat enableDoNotDisturb;
    private View doNotDisturbLayout;
    private RecyclerView ruleList;
    private View noRulesView;
    private View mainLayout;
    private TextView noRulesTextView;
    private FloatingActionButton addRule;

    private String noRules;
    private String rulesDisabled;

    private RuleAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        enableRules = (CheckBox) findViewById(R.id.auto_rules_enable);
        doNotDisturbLayout = findViewById(R.id.dnd_enable_layout);
        enableDoNotDisturb = (SwitchCompat) doNotDisturbLayout.findViewById(R.id.do_not_dist_switch);
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
                    enableDoNotDisturb.setEnabled(true);
                    noRulesTextView.setText(noRules);
                } else {
                    doNotDisturbLayout.animate().alpha(ALPHA_HALF).setDuration(ANIMATION_DURATION).start();
                    enableDoNotDisturb.setEnabled(false);
                    // TODO: 24/06/2017 if no rule is present
                    noRulesTextView.setText(rulesDisabled);
                }
                hideShowAddRule(!isChecked);
            }
        });

        // TODO: 24/06/2017 implement adapter
        ruleList.setLayoutManager(new LinearLayoutManager(this));
        ruleList.setAdapter(adapter);

        addRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 24/06/2017 add dummy rules
            }
        });

        enableDoNotDisturb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                hideShowMainLayout(isChecked);
                hideShowAddRule(isChecked);
            }
        });
    }

    private void hideShowAddRule(boolean wasShown) {
        if(wasShown) { //hide
            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, r.getDisplayMetrics());
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

    private void hideShowNoRule(boolean wasShown) {
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
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            mainLayout.setVisibility(View.VISIBLE);
                        }
                    }).start();
        }
    }
}
