package com.kavir.browser.utils;

import android.content.Context;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class TabManager {
    public static class Tab {
        public WebView view;
        public String title="New Tab";
        public String url="";
        public boolean incognito;
        Tab(WebView v,boolean i){view=v;incognito=i;}
    }

    private final List<Tab> tabs=new ArrayList<>();
    private int currentTab=-1;
    private final Context context;
    private final FrameLayout container;

    public TabManager(Context c,FrameLayout f){context=c;container=f;}

    public Tab newTab(){return newTab(false);}
    public Tab newTab(boolean incognito){
        WebView w=new WebView(context);
        WebSettings s=w.getSettings();
        s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);s.setBuiltInZoomControls(false);
        Tab t=new Tab(w,incognito);tabs.add(t);switchTab(tabs.size()-1);return t;
    }
    public void switchTab(int i){
        if(i<0||i>=tabs.size())return;
        container.removeAllViews();currentTab=i;
        container.addView(tabs.get(i).view,new FrameLayout.LayoutParams(-1,-1));
    }
    public WebView getCurrentTab(){return currentTab>=0&&currentTab<tabs.size()?tabs.get(currentTab).view:newTab().view;}
    public Tab getCurrentInfo(){return currentTab>=0&&currentTab<tabs.size()?tabs.get(currentTab):newTab();}
    public int getCurrentIndex(){return currentTab;}
    public int getTabCount(){return tabs.size();}
    public List<Tab> getTabs(){return tabs;}
    public void closeTab(int i){
        if(i<0||i>=tabs.size())return;
        WebView w=tabs.get(i).view;w.stopLoading();w.destroy();tabs.remove(i);
        if(tabs.isEmpty()){newTab();return;}
        if(currentTab>=tabs.size())currentTab=tabs.size()-1;
        if(currentTab==i)currentTab=Math.max(0,i-1);
        else if(currentTab>i)currentTab--;
        switchTab(currentTab);
    }
}