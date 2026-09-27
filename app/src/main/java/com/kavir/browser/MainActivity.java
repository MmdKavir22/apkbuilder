package com.kavir.browser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.kavir.browser.db.DatabaseHelper;
import com.kavir.browser.proxy.VmessParser;
import com.kavir.browser.proxy.WebViewProxyHelper;
import com.kavir.browser.proxy.XrayCoreManager;
import com.kavir.browser.utils.TabManager;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TabManager tabs;
    private EditText url;
    private ProgressBar progress;
    private TextView indicator;
    private DatabaseHelper db;
    private ImageButton back,forward,refresh,home,menu;
    private Button tabButton;
    private boolean desktopMode=false;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);setContentView(R.layout.activity_main);
        db=new DatabaseHelper(this);
        tabs=new TabManager(this,findViewById(R.id.webViewContainer));
        url=findViewById(R.id.etUrlBar);progress=findViewById(R.id.progressBar);indicator=findViewById(R.id.tvProxyIndicator);
        back=findViewById(R.id.btnBack);forward=findViewById(R.id.btnForward);refresh=findViewById(R.id.btnRefresh);
        home=findViewById(R.id.btnHome);menu=findViewById(R.id.btnMenu);tabButton=findViewById(R.id.btnTabs);

        back.setOnClickListener(v->{WebView w=tabs.getCurrentTab();if(w.canGoBack())w.goBack();});
        forward.setOnClickListener(v->{WebView w=tabs.getCurrentTab();if(w.canGoForward())w.goForward();});
        refresh.setOnClickListener(v->{WebView w=tabs.getCurrentTab();w.reload();});
        home.setOnClickListener(v->navigate("https://www.google.com"));
        menu.setOnClickListener(v->showMenu());
        indicator.setOnClickListener(v->toggleProxy());
        tabButton.setOnClickListener(v->showTabs());

        url.setOnEditorActionListener((v,id,e)->{
            if(id==EditorInfo.IME_ACTION_GO||(e!=null&&e.getKeyCode()==KeyEvent.KEYCODE_ENTER)){navigate(url.getText().toString());return true;}
            return false;
        });

        tabs.newTab();
        navigate("https://www.google.com");
        updateProxyUi();
    }

    private void configureWebView(WebView w){
        WebSettings s=w.getSettings();
        s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setBuiltInZoomControls(false);
        s.setSupportZoom(true);
        if(desktopMode)s.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 Chrome/140 Safari/537.36");
        else s.setUserAgentString(null);

        w.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView v,String u,android.graphics.Bitmap icon){url.setText(u);}
            @Override public void onPageFinished(WebView v,String u){
                url.setText(u);
                TabManager.Tab t=tabs.getCurrentInfo();t.url=u;t.title=v.getTitle()==null||v.getTitle().isEmpty()?"New Tab":v.getTitle();
                if(!t.incognito)db.addHistory(t.title,u);
                updateTabButton();
            }
        });
        w.setWebChromeClient(new WebChromeClient(){
            @Override public void onProgressChanged(WebView v,int p){progress.setVisibility(p>=100?ProgressBar.GONE:ProgressBar.VISIBLE);progress.setProgress(p);}
            @Override public void onReceivedTitle(WebView v,String title){if(tabs.getCurrentIndex()>=0)tabs.getTabs().get(tabs.getCurrentIndex()).title=title;updateTabButton();}
        });
        w.setDownloadListener((u,userAgent,contentDisposition,mimeType,contentLength)->{
            try{
                DownloadManager.Request r=new DownloadManager.Request(Uri.parse(u));
                r.setMimeType(mimeType);r.addRequestHeader("User-Agent",userAgent);
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,guessName(u,contentDisposition));
                ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
                Toast.makeText(this,"دانلود شروع شد",Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(this,"دانلود انجام نشد",Toast.LENGTH_LONG).show();}
        });
    }

    private String guessName(String u,String cd){
        if(cd!=null&&cd.contains("filename=")){String n=cd.substring(cd.indexOf("filename=")+9).replace(""","").trim();if(!n.isEmpty())return n;}
        try{String p=Uri.parse(u).getLastPathSegment();if(p!=null&&!p.isEmpty())return p;}catch(Exception ignored){}
        return "kavir-download";
    }

    private void navigate(String input){
        String s=input==null?"":input.trim();if(s.isEmpty())return;
        if(!s.startsWith("http://")&&!s.startsWith("https://")&&!s.startsWith("file://"))
            s=(s.contains(".")&&!s.contains(" "))?"https://"+s:"https://www.google.com/search?q="+URLEncoder.encode(s,StandardCharsets.UTF_8);
        WebView w=tabs.getCurrentTab();configureWebView(w);w.loadUrl(s);
    }

    private void showMenu(){
        final String[] items={"➕ تب جدید","🕵 تب ناشناس","⭐ افزودن به نشانک‌ها","🔖 نشانک‌ها","🕘 تاریخچه","🔎 جستجو در صفحه","🖥 حالت دسکتاپ","↗ اشتراک‌گذاری صفحه","⚙ تنظیمات"};
        new AlertDialog.Builder(this).setTitle("Kavir Browser").setItems(items,(d,which)->{
            switch(which){
                case 0: tabs.newTab();navigate("https://www.google.com");break;
                case 1: tabs.newTab(true);navigate("https://www.google.com");break;
                case 2:addBookmark();break;
                case 3:showBookmarks();break;
                case 4:showHistory();break;
                case 5:findInPage();break;
                case 6:desktopMode=!desktopMode;configureWebView(tabs.getCurrentTab());tabs.getCurrentTab().reload();Toast.makeText(this,desktopMode?"حالت دسکتاپ روشن شد":"حالت موبایل روشن شد",Toast.LENGTH_SHORT).show();break;
                case 7:sharePage();break;
                case 8:startActivity(new Intent(this,SettingsActivity.class));break;
            }
        }).show();
    }

    private void showTabs(){
        List<TabManager.Tab> list=tabs.getTabs();
        String[] names=new String[list.size()];
        for(int i=0;i<list.size();i++)names[i]=(i==tabs.getCurrentIndex()?"✓ ":"")+(list.get(i).title==null?"New Tab":list.get(i).title);
        new AlertDialog.Builder(this).setTitle("تب‌ها • "+list.size())
            .setItems(names,(d,w)->{tabs.switchTab(w);syncUrl();})
            .setNegativeButton("بستن",null)
            .setPositiveButton("تب جدید", (d,w)->{tabs.newTab();navigate("https://www.google.com");})
            .show();
    }

    private void syncUrl(){WebView w=tabs.getCurrentTab();url.setText(w.getUrl()==null?"":w.getUrl());updateTabButton();}
    private void updateTabButton(){tabButton.setText(String.valueOf(tabs.getTabCount()));}

    private void addBookmark(){
        WebView w=tabs.getCurrentTab();String u=w.getUrl();if(u==null||u.isEmpty())return;
        if(db.isBookmarked(u)){Toast.makeText(this,"این صفحه قبلاً نشانک شده",Toast.LENGTH_SHORT).show();return;}
        db.addBookmark(w.getTitle(),u);Toast.makeText(this,"⭐ به نشانک‌ها اضافه شد",Toast.LENGTH_SHORT).show();
    }

    private void showBookmarks(){
        List<DatabaseHelper.WebItem> items=db.getBookmarks();
        if(items.isEmpty()){Toast.makeText(this,"هنوز نشانکی ندارید",Toast.LENGTH_SHORT).show();return;}
        String[] names=new String[items.size()];
        for(int i=0;i<items.size();i++)names[i]=(items.get(i).title==null||items.get(i).title.isEmpty()?items.get(i).url:items.get(i).title);
        new AlertDialog.Builder(this).setTitle("نشانک‌ها").setItems(names,(d,w)->navigate(items.get(w).url)).setNegativeButton("بستن",null).show();
    }

    private void showHistory(){
        List<DatabaseHelper.WebItem> items=db.getHistory();
        if(items.isEmpty()){Toast.makeText(this,"تاریخچه خالی است",Toast.LENGTH_SHORT).show();return;}
        String[] names=new String[items.size()];
        for(int i=0;i<items.size();i++)names[i]=(items.get(i).title==null||items.get(i).title.isEmpty()?items.get(i).url:items.get(i).title);
        new AlertDialog.Builder(this).setTitle("تاریخچه • 100 مورد اخیر").setItems(names,(d,w)->navigate(items.get(w).url))
            .setNegativeButton("بستن",null).setNeutralButton("پاک کردن",(d,w)->{db.clearHistory();Toast.makeText(this,"تاریخچه پاک شد",Toast.LENGTH_SHORT).show();}).show();
    }

    private void findInPage(){
        final EditText e=new EditText(this);e.setSingleLine(true);e.setHint("متن مورد نظر");
        new AlertDialog.Builder(this).setTitle("جستجو در صفحه").setView(e).setNegativeButton("لغو",null).setPositiveButton("جستجو",(d,w)->{
            String q=e.getText().toString();if(!q.isEmpty())tabs.getCurrentTab().findAllAsync(q);
        }).show();
    }

    private void sharePage(){
        WebView w=tabs.getCurrentTab();Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,(w.getTitle()==null?"":w.getTitle()+"\n")+w.getUrl());startActivity(Intent.createChooser(i,"اشتراک‌گذاری"));
    }

    private void toggleProxy(){
        if(XrayCoreManager.isRunning()){XrayCoreManager.stop();WebViewProxyHelper.setEnabled(false);Toast.makeText(this,"VPN / Xray قطع شد",Toast.LENGTH_SHORT).show();updateProxyUi();return;}
        DatabaseHelper.ConfigItem c=db.getActiveConfig();
        if(c==null){Toast.makeText(this,"ابتدا از تنظیمات یک کانفیگ انتخاب کنید",Toast.LENGTH_LONG).show();startActivity(new Intent(this,ConfigsActivity.class));return;}
        try{
            String json=c.content.trim();if(json.startsWith("vmess://"))json=VmessParser.toXrayJson(json);
            if(XrayCoreManager.start(json)){WebViewProxyHelper.setEnabled(true);Toast.makeText(this,"VPN / Xray وصل شد • "+c.title,Toast.LENGTH_SHORT).show();}
            else{WebViewProxyHelper.setEnabled(false);Toast.makeText(this,"اتصال برقرار نشد؛ کانفیگ یا Xray را بررسی کنید",Toast.LENGTH_LONG).show();}
        }catch(Exception e){WebViewProxyHelper.setEnabled(false);Toast.makeText(this,"خطا: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        updateProxyUi();
    }

    private void updateProxyUi(){
        boolean on=XrayCoreManager.isRunning();WebViewProxyHelper.setEnabled(on);
        indicator.setText(on?"● VPN ON":"DIRECT");
        indicator.setTextColor(getColor(on?R.color.status_proxy:R.color.status_direct));
        indicator.setContentDescription(on?"قطع VPN / Xray":"اتصال VPN / Xray");
    }

    @Override protected void onResume(){super.onResume();updateProxyUi();updateTabButton();if(tabs!=null)syncUrl();}
    @Override public void onBackPressed(){WebView w=tabs.getCurrentTab();if(w.canGoBack())w.goBack();else super.onBackPressed();}
}