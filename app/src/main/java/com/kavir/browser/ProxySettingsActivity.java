package com.kavir.browser;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.kavir.browser.db.DatabaseHelper;
import com.kavir.browser.proxy.VmessParser;
import com.kavir.browser.proxy.WebViewProxyHelper;
import com.kavir.browser.proxy.XrayCoreManager;

public class ProxySettingsActivity extends AppCompatActivity {
    private TextView status,active; private Button toggle; private DatabaseHelper db;
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);setContentView(R.layout.activity_proxy);db=new DatabaseHelper(this);
        status=findViewById(R.id.tvStatus);active=findViewById(R.id.tvActiveConfigTitle);toggle=findViewById(R.id.btnToggleConnection);
        findViewById(R.id.btnManageConfigs).setOnClickListener(v->startActivity(new android.content.Intent(this,ConfigsActivity.class)));
        toggle.setOnClickListener(v->toggle());update();
    }
    private void toggle(){
        if(XrayCoreManager.isRunning()){XrayCoreManager.stop();WebViewProxyHelper.setEnabled(false);Toast.makeText(this,"اتصال قطع شد",Toast.LENGTH_SHORT).show();}
        else{
            DatabaseHelper.ConfigItem c=db.getActiveConfig();if(c==null){Toast.makeText(this,"ابتدا یک کانفیگ فعال انتخاب کنید",Toast.LENGTH_LONG).show();return;}
            try{String json=c.content.trim();if(json.startsWith("vmess://"))json=VmessParser.toXrayJson(json);if(XrayCoreManager.start(json)){WebViewProxyHelper.setEnabled(true);Toast.makeText(this,"Xray متصل شد",Toast.LENGTH_SHORT).show();}else Toast.makeText(this,"کانفیگ یا هسته Xray خطا دارد",Toast.LENGTH_LONG).show();}
            catch(Exception e){Toast.makeText(this,"خطا: "+e.getMessage(),Toast.LENGTH_LONG).show();}
        } update();
    }
    private void update(){DatabaseHelper.ConfigItem c=db.getActiveConfig();active.setText(c==null?"هیچ کانفیگی فعال نیست":c.title);boolean on=XrayCoreManager.isRunning();status.setText(on?"متصل • Xray":"قطع شده");status.setTextColor(getColor(on?R.color.status_direct:R.color.status_disconnected));toggle.setText(on?R.string.disconnect:R.string.connect);}
    @Override protected void onResume(){super.onResume();update();}
}
