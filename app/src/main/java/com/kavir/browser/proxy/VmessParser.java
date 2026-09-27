package com.kavir.browser.proxy;

import android.util.Base64;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;

public final class VmessParser {
    private VmessParser() {}
    public static String toXrayJson(String uri) throws Exception {
        if (uri == null || !uri.trim().startsWith("vmess://")) throw new IllegalArgumentException("VMess URI معتبر نیست");
        String decoded = new String(Base64.decode(pad(uri.trim().substring(8)), Base64.DEFAULT), StandardCharsets.UTF_8);
        JsonObject v = JsonParser.parseString(decoded).getAsJsonObject();
        String address=get(v,"add",""), id=get(v,"id","");
        if(address.isEmpty()||id.isEmpty()) throw new IllegalArgumentException("VMess فاقد server یا UUID است");
        JsonObject root=new JsonObject(); JsonArray ins=new JsonArray();
        JsonObject http=new JsonObject(); http.addProperty("listen","127.0.0.1"); http.addProperty("port",10809); http.addProperty("protocol","http"); ins.add(http);
        JsonObject socks=new JsonObject(); socks.addProperty("listen","127.0.0.1"); socks.addProperty("port",10808); socks.addProperty("protocol","socks"); ins.add(socks); root.add("inbounds",ins);
        JsonObject out=new JsonObject(); out.addProperty("protocol","vmess");
        JsonObject settings=new JsonObject(), node=new JsonObject(); node.addProperty("address",address); node.addProperty("port",getInt(v,"port",443));
        JsonObject user=new JsonObject(); user.addProperty("id",id); user.addProperty("alterId",getInt(v,"aid",0)); user.addProperty("security",get(v,"scy","auto"));
        JsonArray users=new JsonArray(); users.add(user); node.add("users",users); JsonArray vnext=new JsonArray(); vnext.add(node); settings.add("vnext",vnext); out.add("settings",settings);
        JsonObject stream=new JsonObject(); String net=get(v,"net","tcp"), tls=get(v,"tls","");
        stream.addProperty("network",net); if(!tls.isEmpty()) stream.addProperty("security",tls);
        if("ws".equalsIgnoreCase(net)){ JsonObject ws=new JsonObject(); ws.addProperty("path",get(v,"path","/")); String host=get(v,"host",""); if(!host.isEmpty()){JsonObject h=new JsonObject();h.addProperty("Host",host);ws.add("headers",h);} stream.add("wsSettings",ws); }
        if("grpc".equalsIgnoreCase(net)){JsonObject g=new JsonObject();g.addProperty("serviceName",get(v,"path",""));stream.add("grpcSettings",g);}
        String sni=get(v,"sni",""); if(!sni.isEmpty()){JsonObject t=new JsonObject();t.addProperty("serverName",sni);stream.add("tlsSettings",t);}
        out.add("streamSettings",stream); JsonArray outs=new JsonArray(); outs.add(out); JsonObject direct=new JsonObject();direct.addProperty("protocol","freedom");direct.addProperty("tag","direct");outs.add(direct);root.add("outbounds",outs);
        return new Gson().toJson(root);
    }
    private static String pad(String s){int m=s.length()%4;return m==0?s:s+"=".repeat(4-m);}
    private static String get(JsonObject o,String k,String d){return o.has(k)&&!o.get(k).isJsonNull()?o.get(k).getAsString():d;}
    private static int getInt(JsonObject o,String k,int d){try{return o.has(k)?o.get(k).getAsInt():d;}catch(Exception e){return d;}}
}