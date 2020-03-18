package com.example.puff.kimaiaphotolabler;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

public class AppContextSingleton extends AppCompatActivity {
    private static final AppContextSingleton ourInstance = new AppContextSingleton();


    public static AppContextSingleton getInstance() {
        return ourInstance;
    }

    private AppContextSingleton() {
    }

    public Context getApplicationContext() {
       return getApplicationContext();
    }
}