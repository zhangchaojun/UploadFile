package com.example.randomaccessfile.okhttp;


import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 自定义requestbody，显示上传进度
 */
public class OkHttpRequestBody extends RequestBody {
    private RequestBody mRequestBody;
    private BufferedSink mBufferedSink;
    private WeakReference<OkHttpUploadListener> mListenerReference;

    public OkHttpRequestBody(RequestBody requestBody, OkHttpUploadListener uploadListener) {
        this.mRequestBody = requestBody;
        this.mListenerReference = new WeakReference<>(uploadListener);
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (mBufferedSink == null) {
            mBufferedSink = Okio.buffer(sink(sink));
        }
        mRequestBody.writeTo(mBufferedSink);
        mBufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                OkHttpUploadListener listener = mListenerReference.get();
                if (listener != null) {
                    listener.onUploadProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                }
            }
        };
    }
}
