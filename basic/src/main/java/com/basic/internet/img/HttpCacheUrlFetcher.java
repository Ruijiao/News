package com.basic.internet.img;

import com.basic.tool.UtilFile;
import com.bumptech.glide.Priority;
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;
import java.net.URLDecoder;

import okhttp3.OkHttpClient;

/**
 * 仿造OkHttpStreamFetcher，能读取本地缓存的工具
 * 
 * @author Jerry
 *
 */
public class HttpCacheUrlFetcher extends OkHttpStreamFetcher {

	private final GlideUrl url;

	public HttpCacheUrlFetcher(OkHttpClient client, GlideUrl url) {
		super(client, url);
		this.url = url;
	}

	@Override
	public InputStream loadData(Priority priority) throws Exception {
		// 里面方法都在异步线程中
		String imageUrl = url.toStringUrl();
		InputStream stream = null;
		if(imageUrl.startsWith("http://")){
			stream = super.loadData(priority);
		} else {
			imageUrl = URLDecoder.decode(imageUrl, "utf-8");
			stream = UtilFile.loadFile(imageUrl);
		}
		return stream;
	}

}
