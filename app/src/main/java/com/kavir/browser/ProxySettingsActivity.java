package com.kavir.browser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProxySettingsActivity extends AppCompatActivity {
    private SwitchMaterial switchProxy;
    private EditText etWorkerUrl;
    private EditText etSecretToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);

        switchProxy = findViewById(R.id.switchProxy);
        etWorkerUrl = findViewById(R.id.etWorkerUrl);
        etSecretToken = findViewById(R.id.etSecretToken);
        Button btnSaveProxy = findViewById(R.id.btnSaveProxy);

        SharedPreferences prefs = getSharedPreferences("kavir_prefs", MODE_PRIVATE);
        switchProxy.setChecked(prefs.getBoolean("proxy_enabled", false));
        etWorkerUrl.setText(prefs.getString("worker_url", ""));
        etSecretToken.setText(prefs.getString("secret_token", ""));

        btnSaveProxy.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("proxy_enabled", switchProxy.isChecked())
                    .putString("worker_url", etWorkerUrl.getText().toString().trim())
                    .putString("secret_token", etSecretToken.getText().toString().trim())
                    .apply();
            finish();
        });
    }
}
