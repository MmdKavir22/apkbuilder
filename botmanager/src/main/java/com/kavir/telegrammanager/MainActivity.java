package com.kavir.telegrammanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(15,13,22), panel=Color.rgb(29,24,41);
    private final int purple=Color.rgb(157,91,255), red=Color.rgb(255,69,105);
    private final int white=Color.rgb(244,240,250), muted=Color.rgb(180,170,197);
    private LinearLayout root;
    private EditText tokenInput, chatInput, messageInput, messageIdInput, editTextInput;
    private TextView status, botInfo, output, webhookInfo;
    private TokenVault vault;
    private TelegramApi api;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private long nextOffset=0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state); vault=new TokenVault(this); buildUi();
        try { String saved=vault.load(); if(!saved.isEmpty()) tokenInput.setText(saved); }
        catch(Exception e) { toast("توکن ذخیره‌شده خوانده نشد؛ دوباره واردش کن."); }
    }

    private void buildUi() {
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(22),dp(18),dp(28)); root.setBackgroundColor(bg);
        scroll.addView(root); setContentView(scroll);
        root.addView(text("KAVIR BOT MANAGER",22,white,true));
        root.addView(text("پنل محلی مدیریت بات تلگرام",14,muted,false),lp(-1,dp(30)));
        status=text("● هنوز متصل نشده",14,red,true); card(status);
        section("اتصال امن");
        tokenInput=edit("توکن بات از BotFather",true); tokenInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(tokenInput);
        rowButtons(button("اتصال و بررسی توکن",purple,v->connect()),button("پاک‌کردن توکن",red,v->clearToken()));
        botInfo=text("اطلاعات بات بعد از اتصال نمایش داده می‌شود.",13,muted,false); card(botInfo);
        section("ارسال پیام");
        chatInput=edit("Chat ID یا @channel",false); root.addView(chatInput);
        messageInput=edit("متن پیام",false); messageInput.setSingleLine(false); messageInput.setMinLines(2); messageInput.setGravity(Gravity.TOP|Gravity.START); root.addView(messageInput,lp(-1,dp(82)));
        root.addView(button("ارسال پیام",purple,v->send()),lp(-1,dp(48)));
        section("ویرایش یا حذف پیام");
        messageIdInput=edit("Message ID",false); root.addView(messageIdInput);
        editTextInput=edit("متن جدید برای ویرایش",false); root.addView(editTextInput);
        rowButtons(button("ویرایش",purple,v->editMessage()),button("حذف",red,v->deleteMessage()));
        section("پیام‌های دریافتی و کاربران");
        root.addView(button("دریافت آپدیت‌های جدید",purple,v->fetchUpdates()),lp(-1,dp(48)));
        card(text("این بخش فقط آپدیت‌های در دسترس Bot API را نشان می‌دهد؛ تاریخچهٔ کامل چت‌ها قابل دریافت نیست.",12,muted,false));
        output=text("هنوز آپدیتی دریافت نشده.",13,white,false); card(output);
        section("وضعیت وب‌هوک");
        root.addView(button("بررسی وب‌هوک",purple,v->checkWebhook()),lp(-1,dp(48)));
        webhookInfo=text("برای دیدن وضعیت وب‌هوک، ابتدا وصل شو.",13,muted,false); card(webhookInfo);
        root.addView(text("توکن با Android Keystore رمزگذاری می‌شود و روی همین دستگاه ذخیره می‌شود.",12,muted,false),lp(-1,dp(48)));
    }

    private void connect() {
        String token=val(tokenInput);
        if(token.isEmpty()||!token.contains(":")) { toast("توکن معتبر نیست؛ توکن کامل را وارد کن."); return; }
        runApi(()->{
            TelegramApi candidate=new TelegramApi(token);
            JSONObject me=candidate.empty("getMe").getJSONObject("result");
            vault.save(token); api=candidate;
            return "اتصال موفق\nنام: "+me.optString("first_name","—")+"\nیوزرنیم: @"+me.optString("username","—")+"\nشناسه: "+me.optLong("id");
        },s->{status.setText("● متصل شد");status.setTextColor(Color.rgb(92,220,150));botInfo.setText(s);toast("توکن اعتبارسنجی و رمزگذاری شد.");});
    }
    private void clearToken() {
        vault.clear(); tokenInput.setText(""); api=null; status.setText("● توکن پاک شد");status.setTextColor(red);
        botInfo.setText("برای استفاده دوباره توکن را وارد کن."); toast("توکن ذخیره‌شده پاک شد.");
    }
    private TelegramApi getApi() throws Exception {
        if(api!=null)return api; String token=vault.load();
        if(token.isEmpty())throw new Exception("اول توکن را وارد کن و اتصال را بزن.");
        api=new TelegramApi(token);return api;
    }
    private void send() {
        String chat=val(chatInput),msg=val(messageInput);
        if(chat.isEmpty()||msg.isEmpty()){toast("Chat ID و متن پیام را وارد کن.");return;}
        runApi(()->{JSONObject m=getApi().sendMessage(chat,msg).getJSONObject("result");
            return "پیام ارسال شد.\nChat ID: "+m.getJSONObject("chat").optString("id")+"\nMessage ID: "+m.optLong("message_id");},this::showResult);
    }
    private void editMessage() {
        String chat=val(chatInput),id=val(messageIdInput),msg=val(editTextInput);
        if(chat.isEmpty()||id.isEmpty()||msg.isEmpty()){toast("Chat ID، Message ID و متن جدید را وارد کن.");return;}
        runApi(()->{getApi().editMessage(chat,id,msg);return "ویرایش پیام موفق بود." ;},this::showResult);
    }
    private void deleteMessage() {
        String chat=val(chatInput),id=val(messageIdInput);
        if(chat.isEmpty()||id.isEmpty()){toast("Chat ID و Message ID را وارد کن.");return;}
        new AlertDialog.Builder(this).setTitle("حذف پیام").setMessage("از حذف این پیام مطمئنی؟")
            .setNegativeButton("لغو",null).setPositiveButton("حذف",(d,w)->runApi(()->{
                getApi().deleteMessage(chat,id);return "حذف پیام موفق بود.";
            },this::showResult)).show();
    }
    private void fetchUpdates() {
        runApi(()->{
            JSONObject response=getApi().updates(nextOffset); JSONArray arr=response.getJSONArray("result");
            StringBuilder sb=new StringBuilder();
            if(arr.length()==0)return "آپدیت جدیدی در دسترس نیست. اگر وب‌هوک فعال است، ابتدا وضعیت آن را بررسی کن.";
            for(int i=0;i<arr.length();i++){
                JSONObject u=arr.getJSONObject(i); long uid=u.optLong("update_id",0);nextOffset=Math.max(nextOffset,uid+1);
                JSONObject m=u.optJSONObject("message");if(m==null)m=u.optJSONObject("edited_message");
                if(m==null)m=u.optJSONObject("channel_post");if(m==null)m=u.optJSONObject("edited_channel_post");
                if(m==null){sb.append("Update ID: ").append(uid).append("\n──────────────\n");continue;}
                JSONObject chat=m.optJSONObject("chat"),from=m.optJSONObject("from");
                sb.append("Chat ID: ").append(chat==null?"?":chat.optString("id","?")).append("\n");
                if(from!=null){String username=from.optString("username","");String name=(from.optString("first_name","")+" "+from.optString("last_name","")).trim();
                    if(!username.isEmpty())sb.append("User: @").append(username).append("\n");if(!name.isEmpty())sb.append("نام: ").append(name).append("\n");}
                sb.append("Message ID: ").append(m.optLong("message_id")).append("\nمتن: ")
                    .append(m.optString("text","[پیام غیرمتنی]")).append("\n──────────────\n");
            } return sb.toString();
        },this::showResult);
    }
    private void checkWebhook() {
        runApi(()->{JSONObject r=getApi().empty("getWebhookInfo").getJSONObject("result");String url=r.optString("url","");
            return "URL: "+(url.isEmpty()?"تنظیم نشده":url)+"\nPending updates: "+r.optInt("pending_update_count",0)
                +"\nآخرین خطا: "+r.optString("last_error_message","ندارد")+"\n\n"
                +(url.isEmpty()?"وب‌هوک فعال نیست؛ getUpdates می‌تواند کار کند.":"وب‌هوک فعال است؛ getUpdates هم‌زمان با آن کار نمی‌کند.");
        },s->webhookInfo.setText(s));
    }

    private interface Work { String run() throws Exception; }
    private interface Done { void accept(String value); }
    private void runApi(Work work,Done done) {
        status.setText("● در حال ارتباط با تلگرام");status.setTextColor(purple);
        worker.execute(()->{try{String result=work.run();main.post(()->{status.setText("● درخواست موفق");status.setTextColor(Color.rgb(92,220,150));done.accept(result);});}
            catch(Exception e){String msg=e.getMessage()==null?"خطای ناشناخته":e.getMessage();main.post(()->{status.setText("● خطا");status.setTextColor(red);showResult("خطا: "+msg);});}});
    }
    private void showResult(String s){output.setText(s);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private String val(EditText e){return e.getText().toString().trim();}
    private void section(String s){TextView t=text(s,17,white,true);t.setPadding(0,dp(22),0,dp(9));root.addView(t);}
    private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setLineSpacing(dp(3),1f);return t;}
    private EditText edit(String hint,boolean password){EditText e=new EditText(this);e.setTextSize(14);e.setTextColor(white);e.setHintTextColor(muted);e.setHint(hint);e.setPadding(dp(13),dp(8),dp(13),dp(8));e.setBackground(shape(panel,14,Color.rgb(67,52,88)));if(password)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);LinearLayout.LayoutParams p=lp(-1,dp(50));p.bottomMargin=dp(8);e.setLayoutParams(p);return e;}
    private Button button(String label,int color,View.OnClickListener click){Button b=new Button(this);b.setText(label);b.setTextSize(12);b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(shape(color,14,color));b.setOnClickListener(click);return b;}
    private void rowButtons(Button a,Button b){LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);a.setLayoutParams(new LinearLayout.LayoutParams(0,dp(48),1));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(48),1);bp.leftMargin=dp(8);b.setLayoutParams(bp);row.addView(a);row.addView(b);LinearLayout.LayoutParams p=lp(-1,dp(52));p.topMargin=dp(3);root.addView(row,p);}
    private void card(View child){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(14),dp(12),dp(14),dp(12));box.setBackground(shape(panel,16,Color.rgb(49,38,65)));child.setLayoutParams(lp(-1,-2));box.addView(child);LinearLayout.LayoutParams p=lp(-1,-2);p.topMargin=dp(8);p.bottomMargin=dp(6);root.addView(box,p);}
    private GradientDrawable shape(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));d.setStroke(dp(1),stroke);return d;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
    private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
    @Override protected void onDestroy(){super.onDestroy();worker.shutdownNow();}
}
