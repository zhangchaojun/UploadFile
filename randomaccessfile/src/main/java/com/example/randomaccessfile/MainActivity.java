package com.example.randomaccessfile;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.randomaccessfile.asynctask.UploadTask;
import com.example.randomaccessfile.manager.UploadManager;
import com.example.randomaccessfile.okhttp.OkHttpUploadListener;
import com.example.randomaccessfile.okhttp.OkhttpUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.RequestBody;

/**
 * 分块上传文件。
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "cj";
    private static final String SERVER_ADDRESS = "http://192.168.0.105:8090";
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SQLiteExpertSetup.exe";
    private Button bt_upload;
    private ProgressBar progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }


    private void initView() {
        bt_upload = (Button) findViewById(R.id.bt_upload);
        progress = (ProgressBar) findViewById(R.id.progress);

        bt_upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_upload:

                File file = new File(FILE_PATH);
                UploadManager.getInstance().upload(file,SERVER_ADDRESS);

                break;
        }
    }
}
