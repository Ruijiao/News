package com.news.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.news.view.CommonFragment;

import java.util.ArrayList;

/**
 * Created by xiangha on 2016/8/24.
 */

public class AdapterViewPager extends PagerAdapter{

    private ArrayList<CommonFragment> mViews;

    public AdapterViewPager(ArrayList<CommonFragment> arrayView){
        mViews = arrayView;
    }

    @Override
    public int getCount() {
        return mViews.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return mViews.get(position).getView();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }
}
