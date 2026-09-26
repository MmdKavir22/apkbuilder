package com.kavir.worktracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskAdapter.Listener {

    private TextView tvDate;
    private TextView tvStatus;
    private View layoutStatusCard;
    private MaterialButton btnStartWork;
    private MaterialButton btnFinishWork;
    private MaterialButton btnAddTask;
    private MaterialButton btnHistory;
    private RecyclerView rvTasks;

    private final List<Task> taskList = new ArrayList<>();
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDate = findViewById(R.id.tv_date);
        tvStatus = findViewById(R.id.tv_status);
        layoutStatusCard = findViewById(R.id.layout_status_card);
        btnStartWork = findViewById(R.id.btn_start_work);
        btnFinishWork = findViewById(R.id.btn_finish_work);
        btnAddTask = findViewById(R.id.btn_add_task);
        btnHistory = findViewById(R.id.btn_history);
        rvTasks = findViewById(R.id.rv_tasks);

        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, this);
        rvTasks.setAdapter(taskAdapter);

        btnStartWork.setOnClickListener(v -> onStartWork());
        btnFinishWork.setOnClickListener(v -> onFinishWork());
        btnAddTask.setOnClickListener(v -> showAddTaskDialog());
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        tvDate.setText(DataManager.getTodayDisplay());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
        refreshWorkStatus();
    }

    private void loadTasks() {
        taskList.clear();
        taskList.addAll(DataManager.getTasks(this));
        taskAdapter.notifyDataSetChanged();
    }

    private void refreshWorkStatus() {
        boolean active = DataManager.isWorkActive(this);
        btnStartWork.setEnabled(!active);
        btnFinishWork.setEnabled(active);

        if (active) {
            String startDisplay = DataManager.getActiveStartDisplay(this);
            tvStatus.setText(getString(R.string.status_active_prefix, startDisplay));
            layoutStatusCard.setBackgroundResource(R.drawable.bg_status_active);
        } else {
            tvStatus.setText(R.string.status_idle);
            layoutStatusCard.setBackgroundResource(R.drawable.bg_status_idle);
        }
    }

    private void onStartWork() {
        DataManager.startWork(this);
        refreshWorkStatus();
    }

    private void onFinishWork() {
        DataManager.finishWork(this);
        refreshWorkStatus();
    }

    private void showAddTaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        TextInputEditText input = dialogView.findViewById(R.id.et_task_name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_task)
                .setView(dialogView)
                .setPositiveButton(R.string.dialog_add, (dialog, which) -> {
                    String name = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!TextUtils.isEmpty(name)) {
                        DataManager.addTask(this, name);
                        loadTasks();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @Override
    public void onTaskChecked(Task task, boolean checked) {
        DataManager.setTaskChecked(this, task.getId(), checked);
    }

    @Override
    public void onTaskDeleted(Task task) {
        DataManager.deleteTask(this, task.getId());
        loadTasks();
    }
}
