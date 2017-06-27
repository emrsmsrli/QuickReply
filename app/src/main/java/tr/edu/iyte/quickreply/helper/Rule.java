package tr.edu.iyte.quickreply.helper;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tr.edu.iyte.quickreply.R;

public class Rule {
    private String id;
    private String reply;

    private boolean isEnabled;
    private long fromTime;
    private long toTime;
    private List<String> days;

    @SuppressWarnings("unused")
    public Rule() {}

    public Rule(String id, String reply, long fromTime, long toTime, List<String> days) {
        this.id = id;
        this.reply = reply;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.isEnabled = true;
        this.days = new ArrayList<>(days);
    }

    public String getId() {
        return id;
    }

    public String getReply() {
        return reply;
    }

    public List<String> getDays() {
        return days;
    }

    public String getDaysString(Context c) {
        if(days.size() == 1)
            return days.get(0);

        StringBuilder sb = new StringBuilder(32);
        sb.append(days.get(0));
        for(int i = 1; i < days.size() - 1; ++i)
            sb.append(", ").append(days.get(i));
        sb.append(" ")
                .append(c.getString(R.string.and))
                .append(" ")
                .append(days.get(days.size() - 1));
        return sb.toString();
    }

    public String getTimeString(Context c) {
        SimpleDateFormat s = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return s.format(new Date(fromTime))
                + c.getString(R.string.clock_seperator)
                + s.format(new Date(toTime));
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
