package com.basic.internet.img;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import okhttp3.OkHttpClient;

public class HttpCacheUrlLoader implements StreamModelLoader<GlideUrl> {

	private final OkHttpClient client;
	private final String mType;

	public HttpCacheUrlLoader(OkHttpClient client,String type) {
		this.client = client;
		this.mType = type;
	}

	@Override
	public DataFetcher<java.io.InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
		if(UtilLoadImage.SAVE_LONG.equals(mType)){
			return new HttpLongUrlFetcher(client, model);
		}else{
			return new HttpCacheUrlFetcher(client, model);
		}
	}
}
