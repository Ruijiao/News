package com.news.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.news.R;
import com.news.adapter.AdapterViewPager;
import com.news.view.CommonFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private ViewPager mViewPager;
    private LinearLayout mTabLinear;

    private ArrayList<Map<String,String>> mArrayData;
    private ArrayList<CommonFragment> mFragments;
    private ArrayList<View> mTabViews;


    private AdapterViewPager mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_main);

        init();
    }

    private void init(){
        mArrayData = new ArrayList<>();
        mFragments = new ArrayList<>();
        mTabViews = new ArrayList<>();
        mViewPager = (ViewPager)findViewById(R.id.a_main_viewpager);
        mTabLinear = (LinearLayout)findViewById(R.id.a_main_tab_ll);
        initData();
        CommonFragment fragment;
        for(int i = 0; i < mArrayData.size(); i++){
            fragment = new CommonFragment(mArrayData.get(i).get("url"));
            mFragments.add(fragment);
        }
        mAdapter = new AdapterViewPager(mFragments);
        mViewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        LayoutInflater inflater = LayoutInflater.from(this);
        for(int i = 0; i < 3; i++){
            View view = inflater.inflate(R.layout.a_main_tab_item,null);
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(finalI);
                }
            });
            mTabViews.add(view);
            mTabLinear.addView(view);
        }
    }

    private void initData(){
        Map<String,String> map;
        for(int i = 0; i < 3; i++){
            map = new HashMap<>();
            map.put("title","学做菜");
            map.put("url","https://www.baidu.com/");
            mArrayData.add(map);
        }
    }
}
