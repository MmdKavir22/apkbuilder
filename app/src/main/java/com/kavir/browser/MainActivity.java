package com.kavir.browser;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.kavir.browser.db.DatabaseHelper;
import com.kavir.browser.proxy.WebViewProxyHelper;
import com.kavir.browser.proxy.XrayCoreManager;
import com.kavir.browser.utils.TabManager;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private TabManager tabs; private EditText url; private ProgressBar progress; private TextView indicator; private DatabaseHelper db;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b); setContentView(R.layout.activity_main);
        db=new DatabaseHelper(this); tabs=new TabManager(this,findViewById(R.id.webViewContainer));
        url=findViewById(R.id.etUrlBar); progress=findViewById(R.id.progressBar); indicator=findViewById(R.id.tvProxyIndicator);
        ImageButton back=findViewById(R.id.btnBack), forward=findViewById(R.id.btnForward), refresh=findViewById(R.id.btnRefresh), home=findViewById(R.id.btnHome), menu=findViewById(R.id.btnMenu);
        Button tabButton=findViewById(R.id.btnTabs);
        back.setOnClickListener(v->{if(tabs.getCurrentTab().canGoBack())tabs.getCurrentTab().goBack();});
        forward.setOnClickListener(v->{if(tabs.getCurrentTab().canGoForward())tabs.getCurrentTab().goForward();});
        refresh.setOnClickListener(v->tabs.getCurrentTab().reload());
        home.setOnClickListener(v->navigate("https://www.google.com"));
        menu.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));
        tabButton.setOnClickListener(v->{tabs.newTab();navigate("https://www.google.com");tabButton.setText(String.valueOf(tabs.getTabCount()));});
        url.setOnEditorActionListener((v,id,e)->{if(id==EditorInfo.IME_ACTION_GO||(e!=null&&e.getKeyCode()==KeyEvent.KEYCODE_ENTER)){navigate(url.getText().toString());return true;}return false;});
        navigate("https://www.google.com");
    }

    private void navigate(String input){
        String s=input.trim(); if(s.isEmpty())return;
        if(!s.startsWith("http://")&&!s.startsWith("https://"))s=(s.contains(".")&&!s.contains(" "))?"https://"+s:"https://www.google.com/search?q="+URLEncoder.encode(s,StandardCharsets.UTF_8);
        WebView w=tabs.getCurrentTab(); w.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView v,String u){url.setText(u);db.addHistory(v.getTitle(),u);}
        });
        w.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int p){progress.setVisibility(p>=100?ProgressBar.GONE:ProgressBar.VISIBLE);progress.setProgress(p);}});
        w.loadUrl(s);
    }

    @Override protected void onResume(){super.onResume();boolean on=XrayCoreManager.isRunning();WebViewProxyHelper.setEnabled(on);indicator.setText(on?"XRAY ON":"DIRECT");indicator.setTextColor(getColor(on?R.color.status_proxy:R.color.status_direct));}
    @Override protected void onDestroy(){super.onDestroy();}
    @Override public void onBackPressed(){WebView w=tabs.getCurrentTab();if(w.canGoBack())w.goBack();else super.onBackPressed();}
}
