package tr.edu.iyte.quickreply.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.rules.Rule;
import tr.edu.iyte.quickreply.rules.RuleManager;

public class RuleAdapter
        extends RecyclerView.Adapter<RuleAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView time;
        private final TextView reply;
        private final TextView days;
        private final CheckBox enableRule;

        private ViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.time);
            reply = (TextView) itemView.findViewById(R.id.reply_text);
            days = (TextView) itemView.findViewById(R.id.days);
            enableRule = (CheckBox) itemView.findViewById(R.id.enable_rule);
        }
    }

    private List<Rule> rules = new ArrayList<>();
    private Set<Rule> modifiedRules = new HashSet<>();

    public RuleAdapter() {}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rule, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Rule rule = rules.get(position);
        holder.time.setText(rule.getTimeString(holder.itemView.getContext()));
        holder.days.setText(rule.getDaysString(holder.itemView.getContext()));
        holder.reply.setText(rule.getReply());
        
        holder.enableRule.setChecked(rule.isEnabled());
        holder.enableRule.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO: 24/06/2017 disable or enable rule
                Rule r = rules.get(holder.getAdapterPosition());
                r.setEnabled(isChecked);
                modify(r);
            }
        });
    }
    
    public void add(Rule rule) {
        Rule r = RuleManager.checkDates(rule);
        RuleManager.addRule(r);
        rules.add(r);
        notifyItemInserted(rules.size() - 1);
    }

    // only called once, in init. no need to check dates
    public void addAll(List<Rule> rules) {
        this.rules.addAll(rules);
        notifyItemRangeInserted(0, rules.size());
    }
    
    public void remove(int index) {
        RuleManager.deleteRule(rules.get(index).getId());
        rules.remove(index);
        notifyItemRemoved(index);
    }

    public void removeAll() {
        RuleManager.deleteAllRules();
        int size = rules.size();
        rules.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void modify(Rule rule) {
        modifiedRules.add(rule);
        int index;
        for(index = 0; index < rules.size(); ++index)
            if(rules.get(index).getId().equals(rule.getId()))
                break;
        notifyItemChanged(index);
    }

    public void writeOnDiskIfModified() {
        if(!modifiedRules.isEmpty())
            RuleManager.modifyRules(modifiedRules);
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }
}
