package com.kavir.browser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kavir.browser.db.DatabaseHelper;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnProxyConfig = findViewById(R.id.btnProxyConfig);
        Button btnClearData = findViewById(R.id.btnClearData);

        btnProxyConfig.setOnClickListener(v ->
                startActivity(new Intent(this, ProxySettingsActivity.class)));

        btnClearData.setOnClickListener(v -> {
            new DatabaseHelper(this).clearHistory();
            Toast.makeText(this, "تاریخچه پاک شد", Toast.LENGTH_SHORT).show();
        });
    }
}
