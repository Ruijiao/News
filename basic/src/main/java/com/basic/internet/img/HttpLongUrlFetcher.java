package com.basic.internet.img;

import com.basic.tool.UtilFile;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * 仿造OkHttpStreamFetcher，能读取本地缓存的工具
 * 
 * @author Jerry
 *
 */
public class HttpLongUrlFetcher extends HttpCacheUrlFetcher {

	private final GlideUrl url;

	public HttpLongUrlFetcher(OkHttpClient client, GlideUrl url) {
		super(client, url);
		this.url = url;
	}

	@Override
	public InputStream loadData(Priority priority) throws Exception {
		// 里面方法都在异步线程中
		String imageUrl = url.toStringUrl();
		InputStream stream = UtilFile.getFromMyDiskCache(imageUrl, UtilLoadImage.SAVE_LONG);
		if(stream == null){
			byte[] bytes = UtilFile.inputStream2Byte(super.loadData(priority));
			UtilFile.saveToMyDiskCache(url.toStringUrl(), bytes, UtilLoadImage.SAVE_LONG);
			stream = super.loadData(priority);
		}
		return stream;
	}

}
