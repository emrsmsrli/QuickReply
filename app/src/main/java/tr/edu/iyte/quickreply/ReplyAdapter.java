package tr.edu.iyte.quickreply;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReplyAdapter extends BaseAdapter {
    private static class ViewHolder {
        private TextView textView;
    }

    private final List<String> data;
    private final LayoutInflater inflater;

    public ReplyAdapter(Context c, ArrayList<String> data) {
        this.data = data;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder v;
        if(convertView == null) {
            v = new ViewHolder();
            convertView = inflater.inflate(R.layout.reply, null);
            v.textView = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(v);
        } else
            v = (ViewHolder) convertView.getTag();
        v.textView.setText(data.get(position));
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
}
