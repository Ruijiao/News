package com.basic.internet.progress;

/**
 * PackageName : xh.basic.internet
 * Created by MrTrying on 2016/6/2 10:14.
 * E_mail : ztanzeyu@gmail.com
 */
public interface ProgressResponseListener {
    void onResponseProgress(long bytesRead, long contentLength, boolean done);
}
