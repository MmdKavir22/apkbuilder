package com.kavir.worktracker;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private final List<WorkSession> sessions;

    public SessionAdapter(List<WorkSession> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        WorkSession session = sessions.get(position);

        holder.date.setText(holder.itemView.getContext().getString(R.string.session_date, session.getDate()));
        holder.time.setText(holder.itemView.getContext().getString(
                R.string.session_time, session.getStartTime(), session.getFinishTime()));

        if (session.getCompletedTasks().isEmpty()) {
            holder.completedTasks.setText(holder.itemView.getContext().getString(R.string.no_tasks_completed));
        } else {
            holder.completedTasks.setText(TextUtils.join("، ", session.getCompletedTasks()));
        }
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView time;
        TextView completedTasks;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.tv_session_date);
            time = itemView.findViewById(R.id.tv_session_time);
            completedTasks = itemView.findViewById(R.id.tv_completed_tasks);
        }
    }
}
