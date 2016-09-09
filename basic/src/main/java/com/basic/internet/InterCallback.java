package com.basic.internet;

import android.content.Context;

import com.basic.BasicConf;
import com.basic.tool.UtilLog;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


/**
 * 网络框架回调基类，初始化配置。
 * 注意：
 * 1、初始化需要context，请确保整个异步请求过程中context不会消失
 * @author Jerry
 *
 */
public abstract class InterCallback {
	public Context context;
	/**
	 * 请求时间
	 */
	public long requestTime=0;
	
	public InterCallback(Context context){
		this.context = context;
	}
	
	/**
	 * 加载完毕
	 * @param flag
	 * @param url
	 * @param msg
	 */
	public abstract void loaded(int flag,String url, Object msg);
	/**
	 * 返回错误
	 * @param reqCode
	 * @param url
	 * @param obj		原始错误体，网络框架返回的，且一定不能为null
	 * @param backMsg 	向前端提示的错误信息（网络框架内给出的都是""，请在子类根据obj进行设置）
	 * @param backMsg
	 * @param params 请求时的参数：
	 * 	1.请求图片时为null
	 * 	2. get 或 post 没有参数时为 ""
	 * @param cookie
	 */
	public void backResError(int reqCode,String url,Object obj,String backMsg,String method,String params,String cookie){
		UtilLog.print(BasicConf.log_tag_net,"d", "------------------返回错误------------------\n"+url+"\n"+obj);
		//状态404则取消IP预案，用域名访问
		if("404".equals(obj + "") || "403".equals(obj + "")){
			BasicConf.net_domain2ipObj=null;
			BasicConf.net_domain2ipJson="";
		}
		loaded(reqCode, url, backMsg);
		finish();
	}
	
	/**
	 * 返回字符串
	 * @param url
	 * @param str
	 * @param params 请求时的参数：
	 * 	1.请求图片时为null
	 * 	2. get 或 post 没有参数时为 ""
	 * @param cookie
	 */
	public void backResStr(String url,String str,String method,String params,String cookie){
		UtilLog.print(BasicConf.log_tag_net,"d", "------------------返回字符串------------------\n"+url+"\n"+str);
		loaded(UtilInternet.REQ_OK_STRING,url, str);
		finish();
	}
	/**
	 * 返回流
	 * @param url
	 * @param is
	 */
	public void backResIS(String url,InputStream is){
		loaded(UtilInternet.REQ_OK_IS,url, is);
		finish();
	}
	
	/**
	 * 保存服务端返回的cookie
	 * @param cookies
	 */
	public void saveCookie(Map<String,String> cookies,String url,String method) {
		if(!cookies.isEmpty()){
			UtilLog.print(BasicConf.log_tag_net,"d", "------------------接收到cookie------------------\n"+url+"\n"+ cookies.toString());
			for(String key : cookies.keySet()){
				UtilInternet.cookieMap.put(key, cookies.get(key));
			}
		}
	}
	/**
	 * 获取发网络请求的header，属性自动叠加
	 * @param header	已有header，不能为空
	 * @param url		当前url
	 * @param params	参数
	 * @return
	 */
	public Map<String,String> getReqHeader(Map<String,String> header,String url,Map<String,String> params){
		//配置cookie
		String cookie=header.containsKey("Cookie")?header.get("Cookie"):"";
		Iterator<Entry<String,String>> it = UtilInternet.cookieMap.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,String> entry = it.next();
			cookie += entry.getKey() + "=" + entry.getValue() + ";";
		}
		header.put("Cookie",cookie);
		return header;
	}
	
	/**
	 * 结束调用
	 */
	public void finish(){
		context=null;
	}
	
}
