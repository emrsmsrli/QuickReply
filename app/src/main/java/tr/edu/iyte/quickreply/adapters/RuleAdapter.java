package tr.edu.iyte.quickreply.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tr.edu.iyte.quickreply.R;
import tr.edu.iyte.quickreply.helper.Rule;

public class RuleAdapter
        extends RecyclerView.Adapter<RuleAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView time;
        private final TextView days;
        private final ImageButton cancelRule; // TODO remove

        private ViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.time);
            days = (TextView) itemView.findViewById(R.id.days);
            cancelRule = (ImageButton) itemView.findViewById(R.id.cancel);
        }
    }

    private List<Rule> rules = new ArrayList<>();

    public RuleAdapter() {}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rule, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.time.setText(rules.get(position).getTimeString(holder.itemView.getContext()));
        holder.days.setText(rules.get(position).getDaysString());
        
        holder.cancelRule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 24/06/2017
            }
        });
    }
    
    public void add(Rule rule) {
        rules.add(rule);
        notifyItemInserted(rules.size() - 1);
    }

    public void add(List<Rule> rules) {
        this.rules.addAll(rules);
        notifyItemRangeInserted(0, rules.size());
    }
    
    public void remove(int index) {
        rules.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }
}
