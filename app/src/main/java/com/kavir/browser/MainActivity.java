package com.kavir.browser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kavir.browser.db.DatabaseHelper;
import com.kavir.browser.proxy.ProxyWebViewClient;
import com.kavir.browser.utils.TabManager;

public class MainActivity extends AppCompatActivity {
    private TabManager tabManager;
    private EditText etUrlBar;
    private ProgressBar progressBar;
    private TextView tvProxyIndicator;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        tabManager = new TabManager(this, findViewById(R.id.webViewContainer));

        etUrlBar = findViewById(R.id.etUrlBar);
        progressBar = findViewById(R.id.progressBar);
        tvProxyIndicator = findViewById(R.id.tvProxyIndicator);

        setupUI();
        loadHome();
    }

    private void setupUI() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnForward = findViewById(R.id.btnForward);
        ImageButton btnRefresh = findViewById(R.id.btnRefresh);
        ImageButton btnHome = findViewById(R.id.btnHome);
        ImageButton btnMenu = findViewById(R.id.btnMenu);
        Button btnTabs = findViewById(R.id.btnTabs);

        btnBack.setOnClickListener(v -> {
            if (tabManager.getCurrentTab().canGoBack()) tabManager.getCurrentTab().goBack();
        });

        btnForward.setOnClickListener(v -> {
            if (tabManager.getCurrentTab().canGoForward()) tabManager.getCurrentTab().goForward();
        });

        btnRefresh.setOnClickListener(v -> tabManager.getCurrentTab().reload());
        btnHome.setOnClickListener(v -> loadHome());
        btnMenu.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        btnTabs.setOnClickListener(v -> {
            tabManager.newTab();
            loadHome();
            btnTabs.setText(String.valueOf(tabManager.getTabCount()));
        });

        etUrlBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                navigate(etUrlBar.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void loadHome() {
        navigate("https://www.google.com");
    }

    private void navigate(String input) {
        String url = input.trim();
        if (url.isEmpty()) return;

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.contains(".") && !url.contains(" ")) {
                url = "https://" + url;
            } else {
                url = "https://www.google.com/search?q=" +
                        java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        WebView webView = tabManager.getCurrentTab();
        webView.setWebViewClient(new ProxyWebViewClient(this, loadedUrl -> {
            etUrlBar.setText(loadedUrl);
            dbHelper.addHistory(webView.getTitle(), loadedUrl);
        }));

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                } else {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });

        webView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("kavir_prefs", MODE_PRIVATE);
        boolean proxyEnabled = prefs.getBoolean("proxy_enabled", false);
        if (proxyEnabled) {
            tvProxyIndicator.setText("WORKER");
            tvProxyIndicator.setTextColor(getColor(R.color.status_proxy));
        } else {
            tvProxyIndicator.setText("DIRECT");
            tvProxyIndicator.setTextColor(getColor(R.color.status_direct));
        }
    }

    @Override
    public void onBackPressed() {
        WebView current = tabManager.getCurrentTab();
        if (current.canGoBack()) {
            current.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
