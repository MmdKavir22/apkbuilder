package com.kavir.worktracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataManager {

    private static final String PREFS_NAME = "kavir_work_prefs";
    private static final String KEY_TASKS = "tasks_json";
    private static final String KEY_SESSIONS = "sessions_json";
    private static final String KEY_ACTIVE_START_MILLIS = "active_start_millis";
    private static final String KEY_ACTIVE_START_DISPLAY = "active_start_display";
    private static final String KEY_TASK_COUNTER = "task_id_counter";

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static List<Task> getTasks(Context context) {
        List<Task> tasks = new ArrayList<>();
        String json = prefs(context).getString(KEY_TASKS, null);
        if (json == null) {
            tasks = getDefaultTasks();
            saveTasks(context, tasks);
            return tasks;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                tasks.add(new Task(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getBoolean("checked")
                ));
            }
        } catch (JSONException e) {
            tasks = getDefaultTasks();
        }
        return tasks;
    }

    private static List<Task> getDefaultTasks() {
        List<Task> tasks = new ArrayList<>();
        String[] defaults = new String[]{
                "پر کردن بسته‌بندی‌ها",
                "لیبل زدن به بسته‌بندی‌ها",
                "تمیز کردن بسته‌بندی‌ها",
                "پر کردن ظرف سویا",
                "دستمال کشیدن میزها",
                "جارو کشیدن سالن"
        };
        int counter = 0;
        for (String name : defaults) {
            counter++;
            tasks.add(new Task(String.valueOf(counter), name, false));
        }
        return tasks;
    }

    public static void saveTasks(Context context, List<Task> tasks) {
        JSONArray array = new JSONArray();
        try {
            for (Task task : tasks) {
                JSONObject obj = new JSONObject();
                obj.put("id", task.getId());
                obj.put("name", task.getName());
                obj.put("checked", task.isChecked());
                array.put(obj);
            }
        } catch (JSONException e) {
            return;
        }
        prefs(context).edit().putString(KEY_TASKS, array.toString()).apply();
    }

    public static void addTask(Context context, String name) {
        List<Task> tasks = getTasks(context);
        int nextId = prefs(context).getInt(KEY_TASK_COUNTER, tasks.size()) + 1;
        prefs(context).edit().putInt(KEY_TASK_COUNTER, nextId).apply();
        tasks.add(new Task(String.valueOf(nextId), name, false));
        saveTasks(context, tasks);
    }

    public static void deleteTask(Context context, String id) {
        List<Task> tasks = getTasks(context);
        List<Task> updated = new ArrayList<>();
        for (Task task : tasks) {
            if (!task.getId().equals(id)) {
                updated.add(task);
            }
        }
        saveTasks(context, updated);
    }

    public static void setTaskChecked(Context context, String id, boolean checked) {
        List<Task> tasks = getTasks(context);
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                task.setChecked(checked);
                break;
            }
        }
        saveTasks(context, tasks);
    }

    public static List<WorkSession> getSessions(Context context) {
        List<WorkSession> sessions = new ArrayList<>();
        String json = prefs(context).getString(KEY_SESSIONS, null);
        if (json == null) {
            return sessions;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                List<String> completed = new ArrayList<>();
                JSONArray completedArray = obj.getJSONArray("completedTasks");
                for (int j = 0; j < completedArray.length(); j++) {
                    completed.add(completedArray.getString(j));
                }
                sessions.add(new WorkSession(
                        obj.getString("date"),
                        obj.getString("startTime"),
                        obj.getString("finishTime"),
                        completed
                ));
            }
        } catch (JSONException e) {
            return new ArrayList<>();
        }
        Collections.reverse(sessions);
        return sessions;
    }

    private static void addSession(Context context, WorkSession session) {
        JSONArray array;
        String json = prefs(context).getString(KEY_SESSIONS, null);
        try {
            array = json != null ? new JSONArray(json) : new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("date", session.getDate());
            obj.put("startTime", session.getStartTime());
            obj.put("finishTime", session.getFinishTime());
            JSONArray completedArray = new JSONArray();
            for (String name : session.getCompletedTasks()) {
                completedArray.put(name);
            }
            obj.put("completedTasks", completedArray);
            array.put(obj);
        } catch (JSONException e) {
            return;
        }
        prefs(context).edit().putString(KEY_SESSIONS, array.toString()).apply();
    }

    public static boolean isWorkActive(Context context) {
        return prefs(context).getLong(KEY_ACTIVE_START_MILLIS, -1L) != -1L;
    }

    public static String getActiveStartDisplay(Context context) {
        return prefs(context).getString(KEY_ACTIVE_START_DISPLAY, "");
    }

    public static void startWork(Context context) {
        long now = System.currentTimeMillis();
        String display = formatTime(now);
        prefs(context).edit()
                .putLong(KEY_ACTIVE_START_MILLIS, now)
                .putString(KEY_ACTIVE_START_DISPLAY, display)
                .apply();
    }

    public static void finishWork(Context context) {
        long startMillis = prefs(context).getLong(KEY_ACTIVE_START_MILLIS, -1L);
        if (startMillis == -1L) {
            return;
        }
        long now = System.currentTimeMillis();
        String startDisplay = prefs(context).getString(KEY_ACTIVE_START_DISPLAY, formatTime(startMillis));
        String finishDisplay = formatTime(now);
        String dateDisplay = formatDate(startMillis);

        List<String> completedNames = new ArrayList<>();
        for (Task task : getTasks(context)) {
            if (task.isChecked()) {
                completedNames.add(task.getName());
            }
        }

        WorkSession session = new WorkSession(dateDisplay, startDisplay, finishDisplay, completedNames);
        addSession(context, session);

        prefs(context).edit()
                .remove(KEY_ACTIVE_START_MILLIS)
                .remove(KEY_ACTIVE_START_DISPLAY)
                .apply();
    }

    public static String getTodayDisplay() {
        return formatDate(System.currentTimeMillis());
    }

    private static String formatTime(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        return sdf.format(new Date(millis));
    }

    private static String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        return sdf.format(new Date(millis));
    }
}
