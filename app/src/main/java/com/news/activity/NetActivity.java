package com.news.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.basic.internet.UtilInternet;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.load.model.GlideUrl;
import com.news.R;

import java.util.LinkedHashMap;

import config.InternetCallback;
import config.LoadImage;
import config.ReqInternet;
import config.tools.FileManager;
import core.override.MyApplication;


public class NetActivity extends Activity {

    private ImageView imgView,iconView;

    private static String url = "http://s1.cdn.xiangha.com/caipu/201608/2521/252143215152.jpg/MTAwMHgw";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_basic);
        init();
    }

    private void init(){
        imgView = (ImageView)findViewById(R.id.a_setting_img);
        iconView = (ImageView)findViewById(R.id.a_setting_icon);
        BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequest = LoadImage.with(this)
                .load(url)
                .setSaveType(FileManager.save_cache)
                .build();
        if(bitmapRequest != null)
            bitmapRequest.into(imgView);
    }

    private void upLoad(){
        LinkedHashMap<String, String> fileMap = new LinkedHashMap<String, String>();
        fileMap.put("uploadImg_imgs_1", "");
        ReqInternet.in().doPostImg("", fileMap,
                new InternetCallback(MyApplication.in()) {
                    @Override
                    public void loaded(int flag, String url,Object returnObj) {
                        if (flag >= UtilInternet.REQ_OK_STRING) { //成功,回调成功

                        }

                    }
                });
    }
}
