package com.kavir.browser;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.kavir.browser.db.DatabaseHelper;
import com.kavir.browser.proxy.VmessParser;
import com.kavir.browser.proxy.XrayCoreManager;
import java.util.List;

public class ConfigsActivity extends AppCompatActivity {
    private DatabaseHelper db; private LinearLayout list;
    @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_configs);db=new DatabaseHelper(this);list=findViewById(R.id.configList);
        findViewById(R.id.btnAddVmess).setOnClickListener(v->showAdd(false)); findViewById(R.id.btnAddJson).setOnClickListener(v->showAdd(true)); render();}
    private void showAdd(boolean json){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(24,8,24,8);
        EditText title=new EditText(this);title.setHint("نام کانفیگ");
        EditText content=new EditText(this);content.setHint(json?"Xray JSON را Paste کنید":"vmess:// را Paste کنید");content.setMinLines(5);content.setGravity(48);content.setInputType(131073);
        box.addView(title);box.addView(content);
        new android.app.AlertDialog.Builder(this).setTitle(json?"افزودن JSON":"افزودن VMess").setView(box).setNegativeButton("لغو",null).setPositiveButton("ذخیره",(d,w)->{
            String c=content.getText().toString().trim();if(c.isEmpty())return;
            try{if(!json&&!c.startsWith("vmess://"))throw new Exception("لینک VMess معتبر نیست");if(json)XrayCoreManager.test(c);db.addConfig(title.getText().toString().trim().isEmpty()?"Config "+(db.getConfigs().size()+1):title.getText().toString().trim(),c);render();}catch(Exception e){Toast.makeText(this,"کانفیگ نامعتبر است",Toast.LENGTH_LONG).show();}
        }).show();
    }
    private void render(){list.removeAllViews();List<DatabaseHelper.ConfigItem> cs=db.getConfigs();if(cs.isEmpty()){TextView t=new TextView(this);t.setText("هنوز کانفیگی اضافه نشده");t.setTextColor(getColor(R.color.text_muted));t.setPadding(8,30,8,30);list.addView(t);return;}
        for(DatabaseHelper.ConfigItem c:cs){LinearLayout row=new LinearLayout(this);row.setGravity(16);row.setPadding(14,12,8,12);row.setBackgroundResource(R.drawable/bg_card_rounded);
            TextView name=new TextView(this);name.setText((c.active?"✓ ":"")+c.title);name.setTextColor(getColor(c.active?R.color.primary_accent:R.color.text_light));name.setTextSize(16);name.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
            Button use=new Button(this);use.setText(c.active?"فعال":"انتخاب");use.setOnClickListener(v->{db.setActiveConfig(c.id);render();});
            Button del=new Button(this);del.setText("×");del.setOnClickListener(v->{db.deleteConfig(c.id);if(c.active)XrayCoreManager.stop();render();});
            row.addView(name);row.addView(use);row.addView(del);list.addView(row,new LinearLayout.LayoutParams(-1,-2));
            Space s=new Space(this);list.addView(s,new LinearLayout.LayoutParams(1,8));
        }
    }
}
