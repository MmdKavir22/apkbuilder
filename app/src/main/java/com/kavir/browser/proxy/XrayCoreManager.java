package com.kavir.browser.proxy;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import libXray.LibXray;

public final class XrayCoreManager {
    public static final int HTTP_PORT=10809, SOCKS_PORT=10808;
    private static boolean running=false;
    private XrayCoreManager(){}

    public static synchronized boolean start(String configJson){
        try{
            stop();
            JsonObject root=JsonParser.parseString(configJson).getAsJsonObject();
            ensureInbounds(root);
            String cfg=root.toString();
            String test=LibXray.invoke(request("testXray",cfg));
            if(!success(test)){Log.e("KavirXray",test);return false;}
            String result=LibXray.invoke(request("runXray",cfg));
            running=success(result);
            if(!running)Log.e("KavirXray",result);
            return running;
        }catch(Exception e){Log.e("KavirXray","start failed",e);return false;}
    }

    public static synchronized void stop(){
        try{LibXray.invoke("{\"apiVersion\":3,\"method\":\"stopXray\"}");}catch(Throwable ignored){}
        running=false;
    }

    public static synchronized boolean isRunning(){
        try{running=runningState(LibXray.invoke("{\"apiVersion\":3,\"method\":\"getXrayState\"}"));}catch(Throwable ignored){}
        return running;
    }

    public static String test(String configJson){
        try{JsonObject root=JsonParser.parseString(configJson).getAsJsonObject();ensureInbounds(root);return LibXray.invoke(request("testXray",root.toString()));}
        catch(Exception e){return "{\"success\":false,\"error\":"+new Gson().toJson(e.getMessage())+"}";}
    }

    private static String request(String method,String cfg){return "{\"apiVersion\":3,\"method\":"+new Gson().toJson(method)+",\"payload\":{\"xrayJson\":"+new Gson().toJson(cfg)+"}}";}
    private static void ensureInbounds(JsonObject root){
        if(!root.has("inbounds"))root.add("inbounds",new JsonArray());
        JsonArray a=root.getAsJsonArray("inbounds"); boolean http=false,socks=false;
        for(int i=0;i<a.size();i++){JsonObject x=a.get(i).getAsJsonObject();String p=x.has("protocol")?x.get("protocol").getAsString():"";int port=x.has("port")?x.get("port").getAsInt():-1;if("http".equals(p)&&port==HTTP_PORT)http=true;if("socks".equals(p)&&port==SOCKS_PORT)socks=true;}
        if(!http){JsonObject x=new JsonObject();x.addProperty("listen","127.0.0.1");x.addProperty("port",HTTP_PORT);x.addProperty("protocol","http");a.add(x);}
        if(!socks){JsonObject x=new JsonObject();x.addProperty("listen","127.0.0.1");x.addProperty("port",SOCKS_PORT);x.addProperty("protocol","socks");a.add(x);}
    }
    private static boolean success(String s){try{return JsonParser.parseString(s).getAsJsonObject().get("success").getAsBoolean();}catch(Exception e){return false;}}
    private static boolean runningState(String s){try{return JsonParser.parseString(s).getAsJsonObject().getAsJsonObject("data").get("running").getAsBoolean();}catch(Exception e){return false;}}
}