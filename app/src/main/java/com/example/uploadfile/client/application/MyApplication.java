package com.example.uploadfile.client.application;

import android.app.Application;

/**
 * @author zcj
 * @date 2019/9/29
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }
}
