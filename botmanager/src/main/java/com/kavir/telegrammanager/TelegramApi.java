package com.kavir.telegrammanager;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

final class TelegramApi {
    private final String token;
    TelegramApi(String token) { this.token = token.trim(); }

    JSONObject call(String method, JSONObject body) throws Exception {
        URL url = new URL("https://api.telegram.org/bot" + token + "/" + method);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("POST"); c.setConnectTimeout(12000); c.setReadTimeout(18000);
        c.setDoOutput(true); c.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        try {
            byte[] bytes = (body == null ? new JSONObject() : body).toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = c.getOutputStream()) { out.write(bytes); }
            int code = c.getResponseCode();
            InputStream stream = code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line; while ((line = r.readLine()) != null) sb.append(line);
            }
            JSONObject response = new JSONObject(sb.toString());
            if (!response.optBoolean("ok", false))
                throw new Exception(response.optString("description", "Telegram API error"));
            return response;
        } finally { c.disconnect(); }
    }

    JSONObject empty(String method) throws Exception { return call(method, new JSONObject()); }

    JSONObject sendMessage(String chat, String text) throws Exception {
        JSONObject b = new JSONObject(); b.put("chat_id", chat.trim()); b.put("text", text);
        return call("sendMessage", b);
    }

    JSONObject editMessage(String chat, String id, String text) throws Exception {
        JSONObject b = new JSONObject(); b.put("chat_id", chat.trim());
        b.put("message_id", Long.parseLong(id.trim())); b.put("text", text);
        return call("editMessageText", b);
    }

    JSONObject deleteMessage(String chat, String id) throws Exception {
        JSONObject b = new JSONObject(); b.put("chat_id", chat.trim());
        b.put("message_id", Long.parseLong(id.trim())); return call("deleteMessage", b);
    }

    JSONObject updates(long offset) throws Exception {
        JSONObject b = new JSONObject(); b.put("timeout", 0);
        b.put("allowed_updates", new JSONArray().put("message").put("edited_message")
            .put("channel_post").put("edited_channel_post"));
        if (offset > 0) b.put("offset", offset);
        return call("getUpdates", b);
    }
}
