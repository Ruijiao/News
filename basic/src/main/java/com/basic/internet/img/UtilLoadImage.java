package com.basic.internet.img;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

import com.alibaba.sdk.android.httpdns.HttpDns;
import com.alibaba.sdk.android.httpdns.HttpDnsService;
import com.basic.BasicConf;
import com.basic.internet.img.transformation.RoundTransformation;
import com.basic.tool.UtilLog;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.BitmapTypeRequest;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @ClassName GlideImageLoader
 * @ClassDesc
 * @author MrTrying
 * @createTime 2016年4月18日 下午5:23:05
 * @version
 *
 */
public class UtilLoadImage {
	public static final String ACCOUNTID = "136172";
	// 存储图片
	public static final String SAVE_CACHE = "cache";
	public static final String SAVE_LONG = "long";
	public static final String DEFAULT_SAVE_TYPE = SAVE_CACHE;
	
	public static final int SIZE_ORIGINAL = Target.SIZE_ORIGINAL;
	
	private static final DiskCacheStrategy DEFAULT_DISKCACHESTRATEGY = DiskCacheStrategy.SOURCE;



	private OkHttpClient mClient = null;

	public HttpDnsService httpdns;

	public UtilLoadImage(Context context) {
		if (context == null) {
			UtilLog.print("xh_network", "e", "loadImage初始化context为空");
		}
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(BasicConf.net_timeout, TimeUnit.SECONDS);
		builder.writeTimeout(BasicConf.net_timeout, TimeUnit.SECONDS);
		builder.readTimeout(BasicConf.net_timeout * 2, TimeUnit.SECONDS);
		FailedCount.DISTANCE_TIME_MAX = BasicConf.net_timeout * 4;
		mClient = builder.build();
		httpdns = HttpDns.getService(context, ACCOUNTID);
		httpdns.setLogEnabled(true);
		httpdns.setExpiredIPEnabled(false);
		httpdns.setPreResolveAfterNetworkChanged(true);
		Glide.get(context).register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mClient));
	}

	public Builder getBuilder(Activity activty) {
		return new Builder(activty);
	}

	public Builder getBuilder(Context context) {
		return new Builder(context);
	}

	/**
	 * 
	 * @ClassName Builder
	 * @ClassDesc
	 * @author MrTrying
	 * @createTime 2016年5月16日 下午4:33:46
	 * @version
	 *
	 */
	public class Builder {
		private Context mContext;
		private String mUrl = BasicConf.img_defaultPath;
		private int mPlaceholderID = 0;
		private int mErrorID = 0;
		private ArrayList<BitmapTransformation> transformations = new ArrayList<BitmapTransformation>();
		private String mSaveType = DEFAULT_SAVE_TYPE;
		private RequestListener<GlideUrl, Bitmap> mRequestListener = null;

		public Builder(Activity activty) {
			this(activty.getBaseContext());
		}

		public Builder(Context context) {
			this.mContext = context;
			mPlaceholderID = BasicConf.img_placeholderID;
			mErrorID = BasicConf.img_errorID;
		}

		/**
		 * 设置url
		 * @param url
		 * @return
		 */
		public Builder load(String url) {

			this.mUrl = url;
			return this;
		}

		/**
		 * 设置缓存类型
		 * @param saveType
		 * @return
		 */
		public Builder setSaveType(String saveType) {
			this.mSaveType = saveType;
			return this;
		}

		/**
		 * 设置占位图id
		 * @param placeholderId
		 * @return
		 */
		public Builder setPlaceholderId(int placeholderId) {
			this.mPlaceholderID = placeholderId;
			return this;
		}

		/**
		 * 设置错误图片的id
		 * @param errorId
		 * @return
		 */
		public Builder setErrorId(int errorId) {
			this.mErrorID = errorId;
			return this;
		}

		/**
		 * 添加transformation
		 * @param transformation
		 * @return
		 */
		public Builder addBitmapTransformation(BitmapTransformation transformation) {
			if (transformations != null) {
				transformations.add(transformation);
			}
			return this;
		}

		/**
		 * 添加圆角transformation
		 * 
		 * @param roundPx
		 * @return
		 */
		public Builder setImageRound(int roundPx) {
			if(roundPx <= 0){
				return this;
			}
			return addBitmapTransformation(new RoundTransformation(mContext, roundPx));
		}

		/**
		 * 设置监听 
		 * 可以监听加载图片时 onException 和 onResourceReady 的情况
		 * @param listener
		 * @return
		 */
		public Builder setRequestListener(RequestListener<GlideUrl, Bitmap> listener) {
			this.mRequestListener = listener;
			return this;
		}

		/**
		 * 将transformations的集合转换成数组
		 * @return
		 */
		private BitmapTransformation[] toArray() {
			BitmapTransformation[] arrayBitmapTransformation = new BitmapTransformation[transformations.size()];
			transformations.toArray(arrayBitmapTransformation);
			return arrayBitmapTransformation;
		}

		/**
		 * 构建builder
		 * @return 可能为null，必须做判断
		 */
		public BitmapRequestBuilder<GlideUrl, Bitmap> build() {
			BitmapRequestBuilder<GlideUrl, Bitmap> requestBuilder = null;
			BitmapTypeRequest<GlideUrl> request = getCacheRequest(mUrl, mContext,mSaveType).asBitmap();
			if(request == null ){
				return null;
			}
			// 根据缓存类型获取BitmapRequestBuilder
			if (SAVE_CACHE.equals(mSaveType) || SAVE_LONG.equals(mSaveType)) {
				// normal
				requestBuilder =request.diskCacheStrategy(DEFAULT_DISKCACHESTRATEGY);
			} else {
				// no cache
				requestBuilder = request.diskCacheStrategy(DiskCacheStrategy.NONE);
			}
			// 设置Transformation
			BitmapTransformation[] arrayTransformation = toArray();
			if (arrayTransformation.length > 0) {
				requestBuilder.transform(arrayTransformation);
			}
			// 设置占位图
			if (mPlaceholderID > 0) {
				requestBuilder.placeholder(mPlaceholderID);
			}
			// 设置错误占位图
			if (mErrorID > 0) {
				requestBuilder.error(mErrorID);
			}
			// 设置listener
			if (mRequestListener != null) {
				requestBuilder.listener(mRequestListener);
			}
			requestBuilder.dontAnimate();
			return requestBuilder;
		}

		/** 预读 */
		public void preload(){
			BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequestBuilder = build();
			if (bitmapRequestBuilder != null)
				bitmapRequestBuilder.into(SIZE_ORIGINAL, SIZE_ORIGINAL);
		}
		
		public Bitmap getBitmap(){
			Bitmap bitmap = null;
			try {
				BitmapRequestBuilder<GlideUrl, Bitmap> bitmapRequestBuilder = build();
				if (bitmapRequestBuilder != null)
					bitmap =  bitmapRequestBuilder.into(SIZE_ORIGINAL, SIZE_ORIGINAL).get();
				else
					bitmap = Bitmap.createBitmap(0,0, Bitmap.Config.ARGB_8888);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			return bitmap;
		}
		
	}

	/**
	 * 获取glide加载图片器，读取我们的磁盘缓存
	 * 
	 * @param url
	 * @param context
	 * @return
	 */
	private DrawableTypeRequest<GlideUrl> getCacheRequest(String url, Context context,String type) {
		if(TextUtils.isEmpty(url)){
			url = BasicConf.img_defaultPath;
		}
		LazyHeaders headers = getImgHeader(url);
		if(headers.getHeaders().get("url") != null){
			url = headers.getHeaders().get("url");
//			headers.getHeaders().remove("url");
		}
		GlideUrl glideUrl = new GlideUrl(url, headers);
		if(context instanceof Activity){
			Activity activity = (Activity) context;
			if(Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
				return null;
			}
		}
		return Glide.with(context).using(new HttpCacheUrlLoader(mClient,type)).load(glideUrl);
	}

	/**
	 * 获取请求头
	 * 
	 * @return
	 */
	private LazyHeaders getImgHeader(String imageUrl) {
		LazyHeaders.Builder builder = new LazyHeaders.Builder();
		if (BasicConf.net_imgRefererUrl.length() > 0) {
			builder.setHeader("Referer", BasicConf.net_imgRefererUrl);
		}
		if(BasicConf.IMAGE_OPEN_DNS && BasicConf.IMAGE_USE_DNS){
			String theUrl = imageUrl.replace("http://", "").replace("https://", "");
			String[] urls = theUrl.split("/");
			int start = urls[0].indexOf(":");
			if (start > 0)
				urls[0] = theUrl.substring(0, start);
			String ip2 = httpdns.getIpByHostAsync(urls[0]);
			String ip = httpdns.getIpByHost(urls[0]);
			if(!TextUtils.isEmpty(ip)){
				builder.setHeader("Host",urls[0]);
				builder.setHeader("url", imageUrl.replace(urls[0],ip));
			}
		}
		return builder.build();
	}

}
