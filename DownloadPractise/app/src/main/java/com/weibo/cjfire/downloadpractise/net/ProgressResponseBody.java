package com.weibo.cjfire.downloadpractise.net;

import com.weibo.cjfire.downloadpractise.services.DownloadService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by cjfire on 16/10/23.
 */

public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody mResponseBody;
    private final ProgressResponseListener mProgressListener;
    private BufferedSource mBufferedSource;
    private RandomAccessFile raf;
    private FileChannel mChannel;
    private MappedByteBuffer mappedByteBuffer;

    public ProgressResponseBody(ResponseBody mResponseBody, ProgressResponseListener mProgressListener) throws FileNotFoundException {
        this.mResponseBody = mResponseBody;
        this.mProgressListener = mProgressListener;

        File dir = new File(DownloadService.DOWNLOAD_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, "file");
        raf = new RandomAccessFile(file, "rwd");
        try {
            raf.setLength(mResponseBody.contentLength());
            mChannel = raf.getChannel();
            mappedByteBuffer = mChannel.map(FileChannel.MapMode.READ_WRITE, 0, mResponseBody.contentLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {

        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {

        return new ForwardingSource(source) {

            long totalBytesRead = 0L;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {

                long bytesRead = super.read(sink, byteCount);

                InputStream input = sink.inputStream();

                byte[] buffer = new byte[1024];
                int len = -1;

                while ((len = input.read(buffer)) != -1) {
                    mappedByteBuffer.put(buffer, 0 ,len);
                }

                input.close();
                mChannel.close();

                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                mProgressListener.onResponseProgress(totalBytesRead, mResponseBody.contentLength(), bytesRead == -1);

                return bytesRead;
            }
        };
    }
}
