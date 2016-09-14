package core.override;

import android.app.Application;

import config.LoadImage;
import config.ReqInternet;


/**
 * Created by Fang Ruijiao on 2016/9/9.
 */

public class MyApplication extends Application {

    private static MyApplication myApplication;

    public static MyApplication in(){
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        ReqInternet.init(getApplicationContext());
        LoadImage.init(getApplicationContext());
    }
}
