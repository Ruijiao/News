package com.news.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.news.R;

/**
 * Created by xiangha on 2016/8/24.
 */

public class CommonFragment extends Fragment{

    private View mView;
    private String mUrl;

    public CommonFragment(String url){
        mUrl = url;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.a_main_item_common,container,false);
//        WebView webView = (WebView) view.findViewById(R.id.a_main_item_web);
//        webView.loadUrl(mUrl);
//        TextViview.findViewById(R.id.a_main_item_tv);
        return mView;
    }

    public View getView(){
        return mView;
    }
}
