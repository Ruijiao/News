package com.basic.internet.img.module;

import android.content.Context;

import com.basic.BasicConf;
import com.basic.tool.UtilFile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;

/**
 * @ClassName DiskCacheModule
 * @ClassDesc
 * @author MrTrying
 * @createTime 2016年4月18日 下午5:26:02
 * @version
 *
 */
public class DiskCacheModule implements GlideModule {
	@Override
	public void applyOptions(Context context, GlideBuilder builder) {
		// set size & external vs. internal
		builder.setDiskCache(new DiskLruCacheFactory(UtilFile.getSDDir() + "cache/", BasicConf.img_diskCacheSize));
	}

	@Override
	public void registerComponents(Context context, Glide glide) {

	}
}