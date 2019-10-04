package com.example.randomaccessfile.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.example.randomaccessfile.okhttp.OkhttpUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zcj
 * @date 2019/10/4
 */
public class UploadTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "cj";
    //请求地址
    private String url;
    //一块
    private byte[] content;
    private OkhttpUtils.ResponseListener listener;
    private Map<String, String> head = new HashMap<>();

    public UploadTask(String url, byte[] content, String filename,long filePointer, OkhttpUtils.ResponseListener listener) {
        this.url = url;
        this.content = content;
        this.listener = listener;
        head.put("filepointer", String.valueOf(filePointer));
        head.put("filename", filename);
    }

    @Override
    protected Void doInBackground(Void... voids) {

        Log.e(TAG, "doInBackground:开始上传 " );
        OkhttpUtils.getInstance().uploadFile(url, content, head, listener);
        return null;
    }

}
