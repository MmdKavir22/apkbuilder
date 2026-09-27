package com.kavir.browser.proxy;
import android.util.Log;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

public final class WebViewProxyHelper {
    private WebViewProxyHelper(){}
    public static void setEnabled(boolean enabled){
        if(!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)){Log.w("KavirProxy","WebView proxy override unavailable");return;}
        ProxyController c=ProxyController.getInstance();
        if(enabled){
            ProxyConfig config=new ProxyConfig.Builder().addProxyRule("http://127.0.0.1:"+XrayCoreManager.HTTP_PORT).addDirect().build();
            c.setProxyOverride(config,r->{},()->{});
        } else c.clearProxyOverride(r->{},()->{});
    }
}