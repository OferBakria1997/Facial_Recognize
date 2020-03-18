package com.example.puff.kimaiaphotolabler;

import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArduinoSingleton {
    private static final ArduinoSingleton ourInstance = new ArduinoSingleton();

    final String TAG = "ArduinoSingleton"; //for logging
    String requestedAction = "";
    public static ArduinoSingleton getInstance() {
        return ourInstance;
    }

    public static String getArduinoIP() {
        return ArduinoIP;
    }

    public static void setArduinoIP(String arduinoIP) {
        ArduinoIP = arduinoIP;
    }

    public static String ArduinoIP = "";

    private ArduinoSingleton() {
    }

    public void lock() {
        Log.d(TAG,"LOCK REQUESTED");
        requestedAction = "LOCK";
        makeHttpRequest("http://"+ArduinoIP+"/lock");
    }

    public void unlock() {
        Log.d(TAG,"UNLOCK REQUESTED");
        requestedAction = "UNLOCK";
        makeHttpRequest("http://"+ArduinoIP+"/unlock");
    }

    private void makeHttpRequest(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG,requestedAction+" SUCCESS");
                } else {
                    Log.d(TAG,requestedAction+" FAILURE");
                }
            }
        });
    }
}