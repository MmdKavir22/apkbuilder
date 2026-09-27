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
import java.util.HashMap;
import java.util.Map;

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

        String originalUrl = request.getUrl().toString();

        if (!proxyEnabled || workerUrl.isEmpty()) {
            return super.shouldInterceptRequest(view, request);
        }

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            return super.shouldInterceptRequest(view, request);
        }

        // The simple Worker proxy only supports HTTP methods with bodies when
        // the WebView can provide them. GET/HEAD cover normal pages and media.
        if (!request.getMethod().equalsIgnoreCase("GET") &&
                !request.getMethod().equalsIgnoreCase("HEAD")) {
            return super.shouldInterceptRequest(view, request);
        }

        HttpURLConnection connection = null;

        try {
            String separator = workerUrl.contains("?") ? "&" : "?";
            String proxyUrl = workerUrl + separator + "url=" +
                    java.net.URLEncoder.encode(originalUrl, "UTF-8");

            connection = (HttpURLConnection) new URL(proxyUrl).openConnection();

            String secretToken = prefs.getString("secret_token", "");
            if (!secretToken.isEmpty()) {
                connection.setRequestProperty("X-Kavir-Token", secretToken);
            }

            // Forward headers needed by Google/YouTube and especially media.
            for (Map.Entry<String, String> entry : request.getRequestHeaders().entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();

                if (name == null || value == null) continue;

                String lower = name.toLowerCase();
                if (lower.equals("host") || lower.equals("connection") ||
                        lower.equals("content-length")) {
                    continue;
                }

                connection.setRequestProperty(name, value);
            }

            // Explicitly preserve byte-range requests for video/audio.
            String range = request.getRequestHeaders().get("Range");
            if (range != null && !range.isEmpty()) {
                connection.setRequestProperty("Range", range);
            }

            connection.setRequestMethod(request.getMethod());
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            int status = connection.getResponseCode();
            InputStream inputStream = status >= 400
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (inputStream == null) {
                return super.shouldInterceptRequest(view, request);
            }

            String contentType = connection.getContentType();
            String mimeType = "text/html";
            if (contentType != null && !contentType.isEmpty()) {
                mimeType = contentType.split(";")[0].trim();
            }

            String encoding = connection.getContentEncoding();
            if (encoding == null || encoding.isEmpty()) {
                encoding = "UTF-8";
            }

            Map<String, String> responseHeaders = new HashMap<>();

            copyHeader(connection, responseHeaders, "Accept-Ranges");
            copyHeader(connection, responseHeaders, "Content-Length");
            copyHeader(connection, responseHeaders, "Content-Range");
            copyHeader(connection, responseHeaders, "Content-Type");
            copyHeader(connection, responseHeaders, "ETag");
            copyHeader(connection, responseHeaders, "Last-Modified");
            copyHeader(connection, responseHeaders, "Cache-Control");
            copyHeader(connection, responseHeaders, "Expires");

            return new WebResourceResponse(
                    mimeType,
                    encoding,
                    status,
                    connection.getResponseMessage(),
                    responseHeaders,
                    inputStream
            );

        } catch (Exception e) {
            return super.shouldInterceptRequest(view, request);
        }
    }

    private static void copyHeader(
            HttpURLConnection connection,
            Map<String, String> target,
            String name
    ) {
        String value = connection.getHeaderField(name);
        if (value != null) target.put(name, value);
    }
}