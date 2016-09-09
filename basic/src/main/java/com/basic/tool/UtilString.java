package com.basic.tool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class UtilString {

	/**
	 * 获取md5值
	 * @param str 加密字符串
	 * @param if32 是否要32位
	 * @return
	 */
	public static String toMD5(String str, boolean if32) {
		try {
			if (str.length() == 0)
				str = Math.random() + "";
			StringBuffer sb = new StringBuffer(if32 ? 32 : 16);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] array = md5.digest(str.getBytes("utf-8"));

			for (int i = 0; i < array.length; i++) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, if32 ? 3 : 2));
			}
			return sb.toString();
		} catch (Exception e) {
			UtilLog.reportError("md5错误", e);
			return "";
		}
	}
	
	public static String getStringByMap(Map<String,String> map, String sign1 , String sign2){
		String params = "";
		if(map == null){
			return params;
		}
		Iterator<Entry<String,String>> iterator = map.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<String,String> entry = iterator.next();
			params += entry.getKey() + sign2 + entry.getValue() + sign1;
		}
		int start = params.lastIndexOf(sign1);
		if(start >= 0){
			params = params.substring(start, params.length());
		}
		return params;
	}

	/**
	 * IO流转换成String 
	 * @param is
	 * @param encode Encode类型
	 * @return
	 * @throws Exception
	 */
	public static String inputStream2String(InputStream is,String encode) throws Exception{
		StringBuilder sb=new StringBuilder("");
		InputStreamReader streamReader = new InputStreamReader(is, encode);
		sb = new StringBuilder();
		int ch;
		while ((ch = streamReader.read()) != -1) {
			sb.append((char) ch);
		}
		is.close();
		streamReader.close();
		return new String(sb);
	}
	
	/**
	 * 将字符串解析为map
	 * @param str 例如：name=test&url=over解析为map(name:test),map(url:over)的键值数组
	 * @param sign1 map分隔
	 * @param sign2  键值分隔
	 * @return
	 */
	public static LinkedHashMap<String, String> getMapByString(String str, String sign1, String sign2) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		if (str == null)
			return map;
		String[] contents = str.split(sign1);
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == "")
				continue;
			String[] values = contents[i].split(sign2);
			if (values.length != 2)
				continue;
			try {
				String name = URLDecoder.decode(values[0].replace(" ", ""), "UTF-8");
				String value = URLDecoder.decode(values[1], "UTF-8");
				map.put(name, value);
			} catch (Exception e) {
				UtilLog.reportError("将字符串解析为map异常", e);
			}
		}
		return map;
	}
	
	/**
	 * 从json中获取MAP数组
	 * @param json
	 * @return
	 */
	public static ArrayList<Map<String, String>> getListMapByJson(Object json) {
		ArrayList<Map<String, String>> objs = new ArrayList<Map<String, String>>();
		JSONArray array = new JSONArray();
		// 尝试解析
		try {
			if (json == null)
				return objs;
			else if (json.getClass() == JSONArray.class)
				array = (JSONArray) json;
			else if (((String) json).length() == 0)
				return objs;
			else
				array = new JSONArray((String) json);
		} catch (JSONException e1) {
			try {
				array.put(new JSONObject((String) json));
			} catch (JSONException e2) {
				UtilLog.reportError("Json无法解析:"+json, null);
			}
		}
		for (int i = 0; i < array.length(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			try {
				Iterator<?> it = array.getJSONObject(i).keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					Object xx=array.getJSONObject(i).get(key);
					map.put(key, xx.toString());
				}
			} catch (Exception e) {
				// 直接取数组值到map中,key为空
				try {
					map.put("", array.get(i).toString());
				} catch (JSONException e1) {
					UtilLog.reportError("Json无法解析:"+array.toString(), null);
				}
			}
			objs.add(map);
		}
		return objs;
	}
}
