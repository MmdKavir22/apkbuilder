package com.kavir.worktracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface Listener {
        void onTaskChecked(Task task, boolean checked);
        void onTaskDeleted(Task task);
    }

    private final List<Task> tasks;
    private final Listener listener;

    public TaskAdapter(List<Task> tasks, Listener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isChecked());
        holder.taskName.setText(task.getName());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTaskChecked(task, isChecked);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskDeleted(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView taskName;
        TextView deleteButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_task);
            taskName = itemView.findViewById(R.id.tv_task_name);
            deleteButton = itemView.findViewById(R.id.tv_delete_task);
        }
    }
}
