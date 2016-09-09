package com.basic.internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.basic.BasicConf;
import com.basic.tool.UtilFile;
import com.basic.tool.UtilImage;
import com.basic.tool.UtilLog;
import com.basic.tool.UtilString;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UtilInternet {
	/** 请求失败 */
	public static final int REQ_FAILD = 10;
	/** 请求遇到异常 */
	public static final int REQ_EXP = 20;
	/** 请求状态错误 */
	public static final int REQ_STATE_ERROR = 30;
	/** 解析错误 */
	public static final int REQ_STRING_ERROR = 39;
	/** 请求返回code错误 */
	public static final int REQ_CODE_ERROR = 40;
	/** 请求成功 */
	public static final int REQ_OK_STRING = 50;
	public static final int REQ_OK_IS = 70;
	/** 储存cookie的map */
	public static Map<String, String> cookieMap = new HashMap<String, String>();

	private OkHttpClient client = null;

	public UtilInternet(Context context) {
		if (context == null) {
			UtilLog.print("xh_network", "e", "初始化context为空，是否忘记调用init()之类的静态方法");
		}
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(BasicConf.net_timeout , TimeUnit.SECONDS);
		builder.writeTimeout(BasicConf.net_timeout , TimeUnit.SECONDS);
		builder.readTimeout(BasicConf.net_timeout * 2 , TimeUnit.SECONDS);
		client = builder.build();
	}

	public void finish() { }

	/**
	 * GET网络连接
	 * @param url
	 * @param callback
	 */
	public void doGet(String url, final InterCallback callback) {
		String[] urls = url.split("\\?", 2);
		Map<String, String> params = new LinkedHashMap<String, String>();
		if (urls.length == 1) {
			url = urls[0] + "?";
		} else {
			try {
				url = urls[0] + "?"
						+ URLEncoder.encode(urls[1], BasicConf.net_encode).replace("%26", "&").replace("%3D", "=");
			} catch (UnsupportedEncodingException e) {
				callback.backResError(REQ_EXP, url,"encode错误","","doGet","",callback.getReqHeader(new HashMap<String, String>(),url,params).get("Cookie"));
			}
			params = UtilString.getMapByString(urls[1], "&", "=");
		}
		Map<String, String> header = callback.getReqHeader(new HashMap<String, String>(), url, params);
		header = changeHeader(url, header);
		final Handler handler=getResultHandle("Get",url,callback,null,header.get("Cookie"));
		UtilLog.print(BasicConf.log_tag_net, "d", "------------------REQ_GET------------------\n" + url + "\nheader:" + header.toString(), 7);
		Request request = new Request.Builder()
				.url(changeUrlFromHeader(url, header))
				.headers(Headers.of(header))
				.tag(callback.context).build();
		// 里面方法都在异步线程中
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				sendMessage(handler, REQ_FAILD, null);
			}

			@Override
			public void onResponse(Call arg0, Response response) throws IOException {
				sendMessage(handler, REQ_OK_STRING, response);
			}
		});
	}

	/**
	 * 获取流
	 * @param httpUrl
	 * @param callback
	 */
	public void getInputStream(final String httpUrl, final InterCallback callback) {
		Map<String, String> header = callback.getReqHeader(new HashMap<String, String>(), 
				httpUrl,
				new HashMap<String, String>());
		header = changeHeader(httpUrl, header);
		final Handler handler=getResultHandle("getInputStream",httpUrl,callback,null,header.get("Cookie"));
		UtilLog.print(BasicConf.log_tag_net, "d", "------------------REQ_STREAM------------------\n" + httpUrl + "\nheader:" + header.toString());
		Request request = new Request.Builder()
				.url(changeUrlFromHeader(httpUrl, header))
				.headers(Headers.of(header))
				.tag(callback.context).build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException arg1) {
				sendMessage(handler, REQ_FAILD, null);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				sendMessage(handler, REQ_OK_IS, response);
			}
		});
	}

	/**
	 * post提交，带参数param
	 * @param actionUrl 提交url
	 * @param param  参数形如a=1&b=2，如有图片，参数名为“uploadImg_(api参数名)_(图片序号)”。
	 *              图片已根据BarChooseImg自动压缩
	 * @param callback 回调函数
	 */
	public void doPost(String actionUrl, String param, InterCallback callback) {
		LinkedHashMap<String, String> map = UtilString.getMapByString(param, "&", "=");
		doPost(actionUrl, map, callback);
	}

	/**
	 * post提交，带参数param
	 * @param actionUrl 提交url
	 * @param map 参数形如a=1&b=2，如有图片，参数名为“uploadImg_api参数名_序号”。
	 *            图片已根据BarChooseImg自动压缩
	 * @param callback 回调函数
	 */
	public void doPost(final String actionUrl, final LinkedHashMap<String, String> map, final InterCallback callback) {
		final Handler handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what>=REQ_OK_STRING){
					doPost(actionUrl, (RequestBody)msg.obj,map,callback);
				}
				else{
					callback.backResError(msg.what, actionUrl, "", (String)msg.obj,
							"Post",
							UtilString.getStringByMap(map, "&", "="),
							callback.getReqHeader(new HashMap<String, String>(),actionUrl,map).get("Cookie"));
				}
			};
		};
		new Thread(){
			@Override
			public void run() {
				LinkedHashMap<String, String> StringMap = new LinkedHashMap<String, String>();
				//key的参数为“api参数名_文件名_Content-Type”
				LinkedHashMap<String, byte[]> fileMap = new LinkedHashMap<String, byte[]>();
				Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
				String imgName = "file_1";
				try {
					//整理图片和文字
					while (it.hasNext()) {
						Map.Entry<String, String> entry = it.next();
						if(entry.getKey()==null || entry.getValue()==null){
							continue;
						}
						if (entry.getKey().indexOf("uploadImg") == 0) {
							if (entry.getKey().indexOf("uploadImg_") > -1) {
								imgName = entry.getKey().replace("uploadImg_", "");
							}
							if (!TextUtils.isEmpty(entry.getValue())) {
								String imgPath = URLDecoder.decode(entry.getValue(), HTTP.UTF_8);
								CompressFormat format = BasicConf.net_imgUploadJpg?CompressFormat.JPEG: UtilImage.getImgFormat(imgPath);
								String type=format==CompressFormat.PNG?"image/png":"image/jpeg";
								String fileSuffix = format==CompressFormat.PNG?"png":"jpg";
								Bitmap bitmap=UtilImage.imgPathToBitmap(imgPath,BasicConf.net_imgUploadWidth,BasicConf.net_imgUploadHeight,false, null);
								byte[] theByte=UtilImage.bitmapToByte(bitmap, format,BasicConf.net_imgUploadKb);
								if(theByte != null)
									fileMap.put(imgName + "." + fileSuffix + "_" + type, theByte);
							}
						}
						else if(entry.getKey().indexOf("uploadFile") == 0){
							String type="text/xml";
							if (entry.getKey().indexOf("uploadFile_") > -1) {
								imgName = entry.getKey().replace("uploadFile_", "");
							}
							if (!TextUtils.isEmpty(entry.getValue())) {
								byte[] theByte= UtilFile.inputStream2Byte(UtilFile.loadFile(entry.getValue()));
								if(theByte != null)
									fileMap.put(imgName + "_" + type,theByte);
							}
						}
						else StringMap.put(entry.getKey(), entry.getValue());
					}
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendMessage(handler.obtainMessage(REQ_FAILD, "图片出错，请更换后再试"));
					return;
				} catch (Error e) {
					e.printStackTrace();
					System.gc();
					handler.sendMessage(handler.obtainMessage(REQ_FAILD, "图片过大，请更换后再试"));
					return;
				}
				//配置body
				RequestBody formBody=RequestBody.create(MediaType.parse("text/html; charset=utf-8"), "");
				//设置参数
				if(fileMap.isEmpty()){
					FormBody.Builder formBodyBuilder = new FormBody.Builder();
					for (Map.Entry<String, String> entry : StringMap.entrySet()) {
						formBodyBuilder.add(entry.getKey(),entry.getValue());
					}
					formBody = formBodyBuilder.build();
				}
				else{
					MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
					for (Map.Entry<String, String> entry : StringMap.entrySet()) {
						multipartBuilder.addFormDataPart(entry.getKey(),entry.getValue());
					}
					for (Map.Entry<String, byte[]> entry : fileMap.entrySet()) {
						//组装文件流数据
						String[] fileNameSplit = entry.getKey().split("_");
						String contentType = fileNameSplit[fileNameSplit.length - 1];
						StringBuilder fileName = new StringBuilder();
						if(fileNameSplit.length<3){
							callback.backResError(REQ_EXP, actionUrl,"","文件流的key用_无法切割："+entry.getKey(),
								"Post",
								UtilString.getStringByMap(map, "&", "="),
								callback.getReqHeader(new HashMap<String, String>(),actionUrl,map).get("Cookie"));
							return;
						}
						for(int i = 1; i < fileNameSplit.length - 1; i++){
							fileName.append(fileNameSplit[i]);
						}
						multipartBuilder.addFormDataPart(fileNameSplit[0] + "[]", fileName.toString(),RequestBody.create(MediaType.parse(contentType), entry.getValue()));
					}
					formBody=multipartBuilder.build();
				}
				handler.sendMessage(handler.obtainMessage(REQ_OK_STRING, formBody));
			}
		}.start();
	}

	/**
	 * post提交
	 * @param url
	 * @param formBody	
	 * @param params
	 * @param callback
	 */
	public void doPost(final String url, RequestBody formBody,LinkedHashMap<String, String> params,final InterCallback callback) {
		Map<String,String> header = callback.getReqHeader(new HashMap<String, String>(),url,params);
		header=changeHeader(url,header);
		final Handler handler=getResultHandle("Post",url,callback,params,header.get("Cookie"));
		UtilLog.print(BasicConf.log_tag_net,"d","------------------REQ_POST------------------\n"+url+"\n"+params+";\nheader:"+header.toString());
		//创建连接
		Request request=new Request.Builder()
				.url(changeUrlFromHeader(url, header))
				.headers(Headers.of(header))
				.post(formBody)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call,Response response) throws IOException {
				sendMessage(handler, REQ_OK_STRING, response);
			}
			
			@Override
			public void onFailure(Call call, IOException exception) {
				sendMessage(handler, REQ_FAILD, null);
			}
		});
	}



	private static Map<String, String> getCookieMap(Headers headers) {
		List<String> cookieStrings = headers.values("Set-Cookie");
		Map<String, String> cookiesMap = new HashMap<String, String>();
		for (int i = 0; i < cookieStrings.size(); i++) {
			// 保存cookie
			String cookieStr = cookieStrings.get(i);
			int start = cookieStr.indexOf(";");
			if (start > 0) {
				String[] cookies = cookieStr.substring(0, start).split("=");
				if (cookies.length > 1) {
					cookiesMap.put(cookies[0], cookies[1]);
				}
			}
		}
		return cookiesMap;
	}

	public void sendMessage(Handler handler, int state, Response response) {
		Map<String, Object> messageObj = new HashMap<String, Object>();
		if(state == REQ_FAILD){
			messageObj.put("content", "加载失败");
		}else{
			messageObj.put("headers", response.headers());
			if (response.isSuccessful()) {
				try {
					switch (state) {
					case REQ_OK_IS:
						messageObj.put("content", response.body().byteStream());
						break;
					case REQ_OK_STRING:
						messageObj.put("content", response.body().string());
						break;
					}
				} catch (IOException e) {
					state = REQ_EXP;
					messageObj.put("content", e);
				}
			} else {
				state = REQ_CODE_ERROR;
				messageObj.put("content", response.code());
			}
		}
		handler.sendMessage(handler.obtainMessage(state, messageObj));
	}

	/**
	 * 获取解析返回值的handle
	 * @param url
	 * @param callback 可以为空
	 * @return
	 */
	public Handler getResultHandle(final String method,final String url, final InterCallback callback,
			final LinkedHashMap<String, String> params,final String cookie){
		final long startTime = System.currentTimeMillis();
		return new Handler() {
			@Override
			public void handleMessage(Message message) {
				callback.requestTime = System.currentTimeMillis() - startTime;
				if (callback != null) {
					Map<String, Object> messageObj = (Map<String, Object>) message.obj;
					Headers headers = (Headers) messageObj.get("headers");
					//获取参数串信息
					String paramsStr = UtilString.getStringByMap(params, "&", "=");
					switch (message.what) {
					case REQ_OK_IS:
						callback.saveCookie(getCookieMap(headers), url, method);
						callback.backResIS(url, (InputStream) messageObj.get("content"));
						break;
					case REQ_OK_STRING:
						callback.saveCookie(getCookieMap(headers), url, method);
						callback.backResStr(url, (String) messageObj.get("content"),method,paramsStr,cookie);
						break;
					default:
						callback.backResError(message.what, url, messageObj.get("content"), "",method,paramsStr,cookie);
					}
				}
			}
		};
	}

	/**
	 * 读取header中的url
	 * 
	 * @param url
	 * @param header
	 * @return
	 */
	public String changeUrlFromHeader(String url, Map<String, String> header) {
		if (header.containsKey("url")) {
			url = header.get("url");
			header.remove("url");
		}
		return url;
	}

	/**
	 * 获取转换后的header
	 * 
	 * @param url
	 * @return
	 */
	public Map<String, String> changeHeader(String url, Map<String, String> header) {
		header.put("url", url);
		// if(url==StringManager.api_uploadImg){
		// header.put("Host", "api.huher.com");
		// header.put("url", url.replace(StringManager.apiUrl,
		// "http://123.57.189.202:9800/"));
		// header.put("url", url.replace(StringManager.apiUrl,
		// "http://182.92.245.125/"));
		// return header;
		// }
		// 读取配置到BasicConf.net_domain2ipObj
		if (BasicConf.net_domain2ipObj == null) {
			if (BasicConf.net_domain2ipJson.length() > 5) {
				ArrayList<Map<String, String>> list = UtilString.getListMapByJson(BasicConf.net_domain2ipJson);
				if (list.size() > 0) {
					BasicConf.net_domain2ipObj = new HashMap<String, ArrayList<Map<String, String>>>();
					for (Map.Entry<String, String> entry : list.get(0).entrySet()) {
						BasicConf.net_domain2ipObj.put(entry.getKey(), UtilString.getListMapByJson(entry.getValue()));
					}
				}
			}
		}
		if (BasicConf.net_domain2ipObj == null || BasicConf.net_domain2ipObj.size() == 0 || header.containsKey("Host"))
			return header;
		// 替换url为ip
		String theUrl = url.replace("http://", "").replace("https://", "");
		String[] urls = theUrl.split("/");
		int start = urls[0].indexOf(":");
		if (start > 0)
			urls[0] = theUrl.substring(0, start);
		if (BasicConf.net_domain2ipObj.containsKey(urls[0])) {
			ArrayList<Map<String, String>> ipList = BasicConf.net_domain2ipObj.get(urls[0]);
			// 按权重分配
			try {
				if (ipList.size() == 1) {
					header.put("Host", urls[0]);
					header.put("url", url.replace(urls[0], ipList.get(0).get("ip")));
				} else if (ipList.size() > 1) {
					int allWeight = 0;
					for (Map<String, String> ipMap : ipList) {
						allWeight += Integer.parseInt(ipMap.get("weight"));
					}
					long rand = Math.round(Math.random() * allWeight);
					for (Map<String, String> ipMap : ipList) {
						rand -= Integer.parseInt(ipMap.get("weight"));
						if (rand <= 0) {
							ArrayList<Map<String, String>> newList = new ArrayList<Map<String, String>>();
							newList.add(ipMap);
							BasicConf.net_domain2ipObj.put(urls[0], newList);
							break;
						}
					}
				}
			} catch (Exception e) {
				UtilLog.reportError("预案配置异常", e);
			}
		}
		return header;
	}

}
