package com.kavir.worktracker;

import java.util.ArrayList;
import java.util.List;

public class WorkSession {
    private String date;
    private String startTime;
    private String finishTime;
    private List<String> completedTasks;

    public WorkSession(String date, String startTime, String finishTime, List<String> completedTasks) {
        this.date = date;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.completedTasks = completedTasks != null ? completedTasks : new ArrayList<String>();
    }

    public String getDate() {
        return date;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public List<String> getCompletedTasks() {
        return completedTasks;
    }
}
