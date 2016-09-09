package com.basic.internet.progress;

/**
 * PackageName : xh.basic.internet
 * Created by MrTrying on 2016/6/2 10:19.
 * E_mail : ztanzeyu@gmail.com
 */
public interface ProgressRequestListener {
    void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}
