package com.kavir.browser.utils;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class TabManager {
    private final List<WebView> tabs = new ArrayList<>();
    private int currentTab = -1;
    private final Context context;
    private final FrameLayout container;

    public TabManager(Context context, FrameLayout container) {
        this.context = context;
        this.container = container;
    }

    public WebView newTab() {
        WebView webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        tabs.add(webView);
        switchTab(tabs.size() - 1);
        return webView;
    }

    public void switchTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        container.removeAllViews();
        currentTab = index;
        container.addView(tabs.get(currentTab),
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public WebView getCurrentTab() {
        if (currentTab >= 0 && currentTab < tabs.size()) {
            return tabs.get(currentTab);
        }
        return newTab();
    }

    public int getTabCount() {
        return tabs.size();
    }
}
