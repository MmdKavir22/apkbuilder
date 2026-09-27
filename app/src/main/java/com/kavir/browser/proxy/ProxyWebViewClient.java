package com.kavir.browser.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProxyWebViewClient extends WebViewClient {
    private final Context context;
    private final UrlListener listener;

    public interface UrlListener {
        void onUrlLoaded(String url);
    }

    public ProxyWebViewClient(Context context, UrlListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (listener != null) listener.onUrlLoaded(url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        SharedPreferences prefs = context.getSharedPreferences("kavir_prefs", Context.MODE_PRIVATE);
        boolean proxyEnabled = prefs.getBoolean("proxy_enabled", false);
        String workerUrl = prefs.getString("worker_url", "");

        if (!proxyEnabled || workerUrl.isEmpty() || !request.getMethod().equalsIgnoreCase("GET")) {
            return super.shouldInterceptRequest(view, request);
        }

        try {
            String originalUrl = request.getUrl().toString();
            String separator = workerUrl.contains("?") ? "&" : "?";
            String proxyUrl = workerUrl + separator + "url=" +
                    java.net.URLEncoder.encode(originalUrl, "UTF-8");

            URL url = new URL(proxyUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String secretToken = prefs.getString("secret_token", "");
            if (!secretToken.isEmpty()) {
                connection.setRequestProperty("X-Kavir-Token", secretToken);
            }
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.connect();

            String contentType = connection.getContentType();
            String encoding = connection.getContentEncoding();
            if (encoding == null) encoding = "UTF-8";

            InputStream inputStream = connection.getResponseCode() >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (inputStream == null) return super.shouldInterceptRequest(view, request);

            String mimeType = "text/html";
            if (contentType != null) {
                mimeType = contentType.split(";")[0].trim();
            }

            return new WebResourceResponse(mimeType, encoding, inputStream);
        } catch (Exception e) {
            return super.shouldInterceptRequest(view, request);
        }
    }
}
