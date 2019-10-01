package com.example.uploadfile.client.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.uploadfile.R;
import com.example.uploadfile.client.FileUploadClient;
import com.example.uploadfile.entity.FileUploadFile;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "cj";

    private Button bt_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void uploadFile() {
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "hw.apk";
            FileUploadFile uploadFile = new FileUploadFile();
            File file = new File(path);
            String fileMd5 = file.getName();// 文件名
            uploadFile.setFile(file);
            uploadFile.setFile_md5(fileMd5);
            uploadFile.setStarPos(0);// 文件开始位置
            new FileUploadClient().connect(8080, "192.168.137.1", uploadFile);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "uploadFile:  excption: " + e.getMessage());
        }
    }

    private void initView() {
        bt_upload = (Button) findViewById(R.id.bt_upload);

        bt_upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_upload:
                Log.e(TAG, "onClick: ");
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        uploadFile();
                    }
                }.start();
                break;
        }
    }
}
