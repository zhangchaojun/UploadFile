package com.example.randomaccessfile.okhttp;


/**
 * Created by cj on 2017/6/1.
 */
public interface OkHttpUploadListener {

    void onUploadProgress(long current, long total, boolean done);

}
