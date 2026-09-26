package com.kavir.worktracker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView rvHistory = findViewById(R.id.rv_history);
        TextView tvNoHistory = findViewById(R.id.tv_no_history);

        List<WorkSession> sessions = DataManager.getSessions(this);

        if (sessions.isEmpty()) {
            rvHistory.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
        } else {
            rvHistory.setVisibility(View.VISIBLE);
            tvNoHistory.setVisibility(View.GONE);
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            rvHistory.setAdapter(new SessionAdapter(sessions));
        }
    }
}
