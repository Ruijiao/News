package com.basic.internet.progress;

import java.io.Serializable;

/**
 * PackageName : xh.basic.internet.progress
 * Created by MrTrying on 2016/6/2 10:24.
 * E_mail : ztanzeyu@gmail.com
 */
public class ProgressModel implements Serializable {
    //当前读取字节长度
    private long currentBytes;
    //总字节长度
    private long contentLength;
    //是否读取完成
    private boolean done;

    public ProgressModel(long currentBytes, long contentLength, boolean done) {
        this.currentBytes = currentBytes;
        this.contentLength = contentLength;
        this.done = done;
    }

    public long getCurrentBytes() {
        return currentBytes;
    }

    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    @Override
    public String toString() {
        return "ProgressModel{" +
                "currentBytes=" + currentBytes +
                ", contentLength=" + contentLength +
                ", done=" + done +
                "}";
        }
    }
