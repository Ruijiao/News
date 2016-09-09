package com.basic.internet.img;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.basic.BasicConf;
import com.bumptech.glide.request.target.SimpleTarget;

public abstract class BitmapTarget extends SimpleTarget<Bitmap> {

	@Override
	public void onLoadFailed(Exception e, Drawable errorDrawable) {
		super.onLoadFailed(e, errorDrawable);
		if (com.basic.internet.img.FailedCount.count < com.basic.internet.img.FailedCount.FAILED_MAX) {
			if (com.basic.internet.img.FailedCount.count == 0) {
				com.basic.internet.img.FailedCount.failedStartTime = System.currentTimeMillis();
			}
			com.basic.internet.img.FailedCount.count++;
		} else {
			com.basic.internet.img.FailedCount.count = 0;
			long distanceTime = System.currentTimeMillis() - com.basic.internet.img.FailedCount.failedStartTime;
			if (distanceTime < com.basic.internet.img.FailedCount.DISTANCE_TIME_MAX) {
				switchDNS();
				BasicConf.IMAGE_USE_DNS = !BasicConf.IMAGE_USE_DNS;
			}
		}
	}

	public abstract void switchDNS();

}
