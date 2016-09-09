package com.basic.internet;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.basic.BasicConf;
import com.basic.internet.progress.ProgressHelper;
import com.basic.internet.progress.UIProgressRequestListener;
import com.basic.tool.UtilFile;
import com.basic.tool.UtilImage;
import com.basic.tool.UtilLog;
import com.basic.tool.UtilString;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

public class UtilInternetImg extends UtilInternet{
	
	private static volatile UtilInternetImg mUtilInternetImg;
	private OkHttpClient client = null;
	
	private UtilInternetImg(){
		super(null);
		client=(new OkHttpClient()).newBuilder().connectTimeout(BasicConf.net_timeout, TimeUnit.SECONDS)
				.writeTimeout(BasicConf.net_timeout * 6, TimeUnit.SECONDS)
				.readTimeout(BasicConf.net_timeout * 6, TimeUnit.SECONDS)
				.build();
	}
	
	public static UtilInternetImg in(){
		if(mUtilInternetImg == null){
			synchronized (UtilInternetImg.class) {
                if(mUtilInternetImg == null){
                	mUtilInternetImg = new UtilInternetImg();
                }
            }
		}
		return mUtilInternetImg;
	}
	
	/**
	 * post提交，带参数param
	 * @param actionUrl 提交url
	 * @param map
	 *            参数形如a=1&b=2，如有图片，参数名为“uploadImg_api参数名_序号”。
	 *            图片已根据BarChooseImg自动压缩
	 * @param callback 回调函数
	 */
	public void doPost(final String actionUrl, final LinkedHashMap<String, String> map, final InterCallback callback) {
		final Handler handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what>= UtilInternet.REQ_OK_STRING){
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
					handler.sendMessage(handler.obtainMessage(UtilInternet.REQ_FAILD, "图片出错，请更换后再试"));
					return;
				} catch (Error e) {
					e.printStackTrace();
					System.gc();
					handler.sendMessage(handler.obtainMessage(UtilInternet.REQ_FAILD, "图片过大，请更换后再试"));
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
							callback.backResError(UtilInternet.REQ_EXP, actionUrl,"","文件流的key用_无法切割："+entry.getKey(),
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
				handler.sendMessage(handler.obtainMessage(UtilInternet.REQ_OK_STRING, formBody));
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


	public void uploadImageProgress(final String url , final LinkedHashMap<String, String> params ,
	                                final InterCallback callback , final UIProgressRequestListener progressListener){
		final Handler handler=new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if(msg.what>=REQ_OK_STRING){
					uploadImageProgress(url, (RequestBody)msg.obj,params,callback,progressListener);
				}
				else{
					callback.backResError(msg.what, url, "", (String)msg.obj,
							"Post",
							UtilString.getStringByMap(params, "&", "="),
							callback.getReqHeader(new HashMap<String, String>(),url,params).get("Cookie"));
				}
			};
		};
		new Thread(){
			@Override
			public void run() {
				LinkedHashMap<String, String> StringMap = new LinkedHashMap<String, String>();
				//key的参数为“api参数名_文件名_Content-Type”
				LinkedHashMap<String, byte[]> fileMap = new LinkedHashMap<String, byte[]>();
				Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
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
								CompressFormat format = BasicConf.net_imgUploadJpg?CompressFormat.JPEG:UtilImage.getImgFormat(imgPath);
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
								byte[] theByte=UtilFile.inputStream2Byte(UtilFile.loadFile(entry.getValue()));
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
							callback.backResError(REQ_EXP, url,"","文件流的key用_无法切割："+entry.getKey(),
									"Post",
									UtilString.getStringByMap(params, "&", "="),
									callback.getReqHeader(new HashMap<String, String>(),url,params).get("Cookie"));
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

	private void uploadImageProgress(final String url , RequestBody formBody , final LinkedHashMap<String, String> params ,
	                                 final InterCallback callback , UIProgressRequestListener progressListener){
		Map<String, String> header = callback.getReqHeader(new HashMap<String, String>(), url, params);
		header = changeHeader(url, header);
		final Handler handler=getResultHandle("Post",url,callback,params,header.get("Cookie"));
		UtilLog.print(BasicConf.log_tag_net, "d", "------------------REQ_POST------------------\n" + url + "\n" + params + ";\nheader:" + header.toString());
		// 创建连接
		final Request request = new Request.Builder()
				.url(url)
				.headers(Headers.of(header))
				.post(ProgressHelper.addProgressRequestListener(formBody, progressListener))
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException arg1) {
				sendMessage(handler, REQ_FAILD, null);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				sendMessage(handler, REQ_OK_STRING, response);
			}
		});
	}

}
