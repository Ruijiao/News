package config;

import android.content.Context;
import android.util.Log;

import com.basic.internet.InterCallback;

import java.util.ArrayList;
import java.util.Map;

import config.tools.StringManager;


/**
 * 网络请求回调类
 * 
 * @author Jerry
 */
public abstract class InternetCallback extends InterCallback {

	public InternetCallback(Context context) {
		super(context);
	}

	@Override
	public void backResStr(String url, String str, String method, String params, String cookie) {
		Log.i(XHConf.log_tag_net, "------------------返回字符串------------------\n" + url + "\n" + str);
		String msg = "";
		// 解析API中的res与data
		if (url.contains("http")) {
			ArrayList<Map<String, String>> resultList = StringManager.getListMapByJson(str);
			// 解析过程
			if (resultList.size() > 0) {
				Map<String, String> result = resultList.get(0);
				try {
					String resCode = result.get("res");
					String resData = result.get("data");
					msg = result.get("data");
					if (msg.equals("网络不稳定")) {
						msg = "网络不稳定，请重试";
						loaded(ReqInternet.REQ_CODE_ERROR, url, msg);
					} else
						loaded(ReqInternet.REQ_CODE_ERROR, url, msg);
					/** 测试代码 */
				} catch (Exception e) {
					e.printStackTrace();
					msg = "数据展示异常，请反馈给我们";
					loaded(ReqInternet.REQ_STRING_ERROR, url, msg);
				}
			} else {
				msg = "解析错误，请重试或反馈给我们";
				loaded(ReqInternet.REQ_STRING_ERROR, url, msg);
			}
		} else
			loaded(ReqInternet.REQ_OK_STRING, url, str);
		finish();
	}

	@Override
	public void backResError(int reqCode, String url, Object obj, String backMsg, String method, String params, String cookie) {
		String[] values = url.split("\\?", 2);
		String theUrl = values[0];
		String statMsg = "", statContent = theUrl + "_";
		switch (reqCode) {
		case ReqInternet.REQ_FAILD:
			backMsg = "网络错误，请检查网络或重试";
			statMsg = "网络错误";
			statContent += backMsg;
			break;
		case ReqInternet.REQ_EXP:
			Exception e = (Exception) obj;
			backMsg = "连接异常，请检查网络或重试";
			statMsg = "连接异常";
			String expMsg = e.getMessage();
			statContent += expMsg == null ? e.toString() : expMsg;
			break;
		case ReqInternet.REQ_STATE_ERROR:
			backMsg = "状态错误" + obj.toString() + "，请重试";
			statMsg = "服务状态错误";
			statContent += obj.toString();
			break;
		}
		super.backResError(reqCode, url, obj, backMsg, method, params, cookie);
	}

	@Override
	public void saveCookie(Map<String, String> cookies, String url, String method) {
		for (String name : cookies.keySet()) {
			String value = cookies.get(name);
			// 保存cookie
			if (name.equals("USERID") && value.length() > 0) {
				ReqInternet.cookieMap.put(name, value);
				break;
			}
		}
	}

	@Override
	public Map<String, String> getReqHeader(Map<String, String> header, String url, Map<String, String> params) {
		// 配置cookie
		String cookie = header.containsKey("Cookie") ? header.get("Cookie") : "";

		header.put("Cookie", cookie);

		if (!header.containsKey("Connection"))
			header.put("Connection", "keep-alive");
		if (!header.containsKey("Charset"))
			header.put("Charset", XHConf.net_encode);

		return super.getReqHeader(header, url, params);
	}
}
