package tr.edu.iyte.quickreply.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tr.edu.iyte.quickreply.QuickReplyTile;
import tr.edu.iyte.quickreply.R;

public class ReplyAdapter extends BaseAdapter {
    private static class ViewHolder {
        private TextView textView;
        private View replyLayout;

        private ViewHolder(View v) {
            textView = (TextView) v.findViewById(R.id.reply_text);
            replyLayout = v.findViewById(R.id.reply_layout);
        }
    }

    private final List<String> data;
    private final LayoutInflater inflater;
    private final View.OnTouchListener tListener;

    public ReplyAdapter(Context c, View.OnTouchListener tListener, ArrayList<String> data) {
        this.data = data;
        this.tListener = tListener;
        inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder v;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.reply, null);
            v = new ViewHolder(convertView);
            convertView.setTag(v);
        } else
            v = (ViewHolder) convertView.getTag();

        v.textView.setText(data.get(position));
        v.replyLayout.setOnTouchListener(tListener);

        return convertView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public void add(String item) {
        data.add(item);
        notifyDataSetChanged();
    }

    public void remove(int idx) {
        QuickReplyTile.removeReply(data.get(idx));
        data.remove(idx);
        notifyDataSetChanged();
    }
}
