package tr.edu.iyte.quickreply.helper;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tr.edu.iyte.quickreply.R;

public class Rule {
    // TODO: 24/06/2017 will gson converter see these?
    private String id;
    private long fromTime;
    private long toTime;
    private List<String> days;

    public Rule() {}

    public Rule(String id, long fromTime, long toTime, List<String> days) {
        this.id = id;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.days = new ArrayList<>(days);
    }

    public String getId() {
        return id;
    }

    public List<String> getDays() {
        return days;
    }

    public String getDaysString() {
        String d = days.toString();
        return d.substring(1, d.length() - 1); // remove '[' and ']'
    }

    public String getTimeString(Context c) { // TODO: 24/06/2017 check if format is true?
        SimpleDateFormat s = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return s.format(new Date(fromTime))
                + c.getString(R.string.clock_seperator)
                + s.format(new Date(toTime));
    }
}
