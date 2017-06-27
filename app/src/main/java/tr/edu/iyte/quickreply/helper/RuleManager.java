package tr.edu.iyte.quickreply.helper;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import tr.edu.iyte.quickreply.R;

public class RuleManager {
    public interface RuleReadListener {
        void onRulesRead(List<Rule> rules);
    }

    private static final String TAG = "RuleManager";
    private static final String RULE_PATH = "rules";
    private static final int MAX_DAYS = 2;
    private static final Gson GSON = new Gson();

    private static File ruleDirectory;

    private static String everyDay;
    private static String weekDays;
    private static String weekendDays;
    
    private static ArrayList<String> weekDaysList = new ArrayList<>();
    private static ArrayList<String> shortWeekDaysList = new ArrayList<>();
    private static ArrayList<String> weekendDaysList = new ArrayList<>();
    private static ArrayList<String> shortWeekendDaysList = new ArrayList<>();

    private static List<Rule> rules = null;

    private RuleManager() {}

    public static void init(Context c) {
        if(ruleDirectory == null) {
            ruleDirectory = new File(c.getFilesDir(), RULE_PATH);
            if(!ruleDirectory.exists() && !ruleDirectory.mkdirs())
                throw new RuntimeException("cannot make rule directory");
            Resources r = c.getResources();

            everyDay = c.getString(R.string.everyday);
            weekDays = c.getString(R.string.weekdays);
            weekendDays = c.getString(R.string.weekends);

            Collections.addAll(weekDaysList, r.getStringArray(R.array.weekdays_array));
            Collections.addAll(shortWeekDaysList, r.getStringArray(R.array.weekdays_s_array));
            Collections.addAll(weekendDaysList, r.getStringArray(R.array.weekends_array));
            Collections.addAll(shortWeekendDaysList, r.getStringArray(R.array.weekends_s_array));

            getRules(null);
            Log.i(TAG, "RuleManager init complete");
        }
    }

    public static boolean hasRules() {
        return rules != null && !rules.isEmpty();
    }
    
    public static Rule checkDates(Rule rule) {
        List<String> ruleDays = rule.getDays();

        if(ruleDays.containsAll(weekDaysList)) {
            ruleDays.removeAll(weekDaysList);
            ruleDays.add(weekDays);
        } 
        
        if(ruleDays.containsAll(weekendDaysList)) {
            ruleDays.removeAll(weekendDaysList);
            ruleDays.add(weekendDays);
        }

        if(ruleDays.contains(weekDays) && ruleDays.contains(weekendDays)) {
            ruleDays.remove(weekDays);
            ruleDays.remove(weekendDays);
            ruleDays.add(everyDay);
            return rule;
        }
        
        if(ruleDays.size() > MAX_DAYS) {
            for(int i = 0; i < ruleDays.size(); ++i) {
                String day = ruleDays.get(i);
                int indexToInsert = ruleDays.indexOf(day);

                // don't process these two strings as they're ranges of days
                if(day.equals(weekDays) || day.equals(weekendDays))
                    continue;

                ruleDays.remove(indexToInsert);
                int indexToGet;
                if((indexToGet = weekDaysList.indexOf(day)) != -1
                        || (indexToGet = weekendDaysList.indexOf(day)) != -1) {
                    ruleDays.add(indexToInsert,
                            weekDaysList.contains(day) ?
                                    shortWeekDaysList.get(indexToGet) :
                                    shortWeekendDaysList.get(indexToGet));
                }
            }
        }
        
        return rule;
    }

    public static void addRule(final Rule rule) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String id = rule.getId();
                try(PrintWriter pw = new PrintWriter(new File(ruleDirectory, id))) {
                    pw.print(GSON.toJson(rule));
                    pw.flush();
                    rules.add(rule);
                    Log.i(TAG, "rule added: " + id);
                } catch(FileNotFoundException e) {
                    Log.wtf(TAG, "addRule: fileNotFound", e);
                }
            }
        }).start();
    }

    private static void modifyRules(final Set<Rule> rules) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(Rule rule : rules) {
                    String id = rule.getId();
                    try(PrintWriter pw = new PrintWriter(new File(ruleDirectory, id))) {
                        pw.print(GSON.toJson(rule));
                        pw.flush();
                        Log.i(TAG, "rule modified: " + id);
                    } catch(FileNotFoundException e) {
                        Log.wtf(TAG, "modifyRules: fileNotFound", e);
                    }
                }
            }
        }).start();
    }

    public static void deleteRule(String id) {
        File ruleFile = new File(ruleDirectory, id);
        if(ruleFile.exists())
            if(!ruleFile.delete())
                Log.w(TAG, "rule not deleted: " + id);
    }

    public static void deleteAllRules() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] ruleFiles = ruleDirectory.listFiles();

                rules.clear();
                for(File ruleFile : ruleFiles)
                    if(!ruleFile.delete())
                        Log.w(TAG, "rule not deleted: " + ruleFile.getName());
            }
        }).start();
    }

    public static void getRules(final RuleReadListener listener) {
        if(rules != null) {
            listener.onRulesRead(rules);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                File[] ruleFiles = ruleDirectory.listFiles();
                rules = new ArrayList<>();

                for(File ruleFile : ruleFiles) {
                    try(Scanner scanner = new Scanner(ruleFile)) {
                        rules.add(GSON.fromJson(scanner.nextLine(), Rule.class));
                    } catch(FileNotFoundException e) {
                        Log.wtf(TAG, "getRules: fileNotFound", e);
                    }
                }

                if(listener != null)
                    listener.onRulesRead(rules);

            }
        }).start();
    }
}
