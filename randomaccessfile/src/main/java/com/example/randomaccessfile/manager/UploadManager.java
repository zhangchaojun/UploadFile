package com.example.randomaccessfile.manager;

import android.util.Log;

import com.example.randomaccessfile.asynctask.UploadTask;
import com.example.randomaccessfile.okhttp.OkhttpUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author zcj
 * @date 2019/10/4
 * 同时只能开启十个线程进行上传任务
 */
public class UploadManager {

    private static UploadManager instance;
    private final String TAG = "cj";
    private final int max_thread = 10;
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    private File file;
    private String url;
    private long blockSize = 1024 * 1024;
    private byte[] buffer;


    public static UploadManager getInstance() {
        if (instance == null) {
            instance = new UploadManager();
        }
        return instance;
    }


    public void upload(File file, String url) {
        this.file = file;
        this.url = url;

        int blockCount = getBlockCount(file.length(), blockSize);
        for (int i = 0; i < blockCount; i++) {

            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(i * blockSize);
                if (i == blockCount - 1) {
                    long lastBlockFileSize = getLastBlockFileSize(file.length(), blockSize);
                    Log.e(TAG, "最后一块 size ==" + lastBlockFileSize);
                    buffer = new byte[(int) lastBlockFileSize];
                } else {
                    Log.e(TAG, "第" + i + "块 size == " + blockSize);
                    buffer = new byte[(int) blockSize];
                }
                randomAccessFile.read(buffer);
                final int finalI = i;
                UploadTask uploadTask = new UploadTask(url, buffer, file.getName(), i * blockSize, new OkhttpUtils.ResponseListener() {
                    @Override
                    public void success(String str) {
                        Log.e(TAG, "success: 上传成功 == " + finalI);
                    }

                    @Override
                    public void fail(String error) {

                    }
                });
                uploadTask.executeOnExecutor(executor);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    /**
     * 根据文件总长度，以及定义的每块长度，获取最后一块的长度
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块长度
     * @return 最后一块的长度
     */
    private long getLastBlockFileSize(long totalFileSize, long blockFileSize) {
        if (totalFileSize <= 0 || blockFileSize <= 0) {
            return 0;
        }
        long lastBlockFileSize = totalFileSize % blockFileSize;
        return lastBlockFileSize == 0 ? blockFileSize : lastBlockFileSize;
    }

    /**
     * 根据文件总长度，每块文件长度 获取块数
     *
     * @param totalFileSize 文件总长度
     * @param blockFileSize 每块文件长度
     * @return int 总块数
     */
    private int getBlockCount(long totalFileSize, long blockFileSize) {
        // 若所传数据均不合法，则默认为一块
        if (totalFileSize <= 0 || blockFileSize <= 0 || totalFileSize <= blockFileSize) {
            return 1;
        }
        // 是否有余数
        boolean hasRemainder = (totalFileSize % blockFileSize) != 0;
        int blockSize = (int) (totalFileSize / blockFileSize);
        return hasRemainder ? blockSize + 1 : blockSize;
    }

}
