package com.basic.tool;

import android.annotation.SuppressLint;
import android.util.Log;

import com.basic.BasicConf;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilLog {
	/**
	 * Exception日志记录（可记录到error_log.txt）
	 * 
	 * @param str 错误自定义信息。
	 * @param Exception
	 *            异常，会记录异常堆栈。如果异常非常明显可以只写清楚str即可，自己能定位到就ok
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String reportError(String str, Exception e) {
		String msg = str + "---";
		if (e != null) {
			StackTraceElement[] excep = e.getStackTrace();
			msg += "\r\n" + e.toString() + "\r\n" + e.getMessage();
			for (int i = 0; i < 30 && i < excep.length; i++) {
				msg += "\r\n" + excep[i].getFileName() + "-" + excep[i].getMethodName() + "()-" + excep[i].getLineNumber();
			}
		} else {
			StackTraceElement[] excep = (new Exception()).getStackTrace();
			for (int i = 1; i < 5 && i < excep.length; i++) {
				msg += "\r\n" + excep[i].getFileName() + "-" + excep[i].getMethodName() + "()-" + excep[i].getLineNumber();
			}
		}
		// 写入系统日志
		if (BasicConf.log_save2file) {
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			String msgStr = sdf.format(new Date()) + "-E-" + msg + "\r\n\r\n";
			ByteArrayInputStream stream = new ByteArrayInputStream(msgStr.getBytes());
			UtilFile.saveFileToCompletePath(UtilFile.getSDDir() + "error_log.txt", stream, true);
		}

		if (BasicConf.log_isDebug)
			Log.e(BasicConf.log_tag_default, msg);
		return msg;
	}

	/**
	 * 输出信息
	 * 
	 * @param type
	 *            i,d,(w将输出到系统日志warn_log.txt)
	 * @param str
	 *            输出信息
	 * @param stack
	 *            堆栈数，默认为2即调用打印函数的语句位置
	 */
	public static String print(String tag, String type, String str, int stack) {
		if (BasicConf.log_isDebug) {
			StackTraceElement[] excep = (new Exception()).getStackTrace();
			int start = 1;
			SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
			String msg = str + ":";
			if (excep[1].getMethodName().equals("print")) {
				// 1层堆栈在本文件函数
				start++;
				stack++;
			}
			for (int i = start; i < stack && i < excep.length; i++) {
				msg += "\r\n" + excep[i].getFileName() + "-" + excep[i].getMethodName() + "()-"
						+ excep[i].getLineNumber();
			}
			if (type == "i") {
				Log.i(BasicConf.log_tag_all, msg);
				if (tag.length() > 0)
					Log.i(tag, msg);
				else
					Log.i(BasicConf.log_tag_default, msg);
			} else if (type == "d") {
				Log.d(BasicConf.log_tag_all, msg);
				if (tag.length() > 0)
					Log.d(tag, msg);
				else
					Log.d(BasicConf.log_tag_default, msg);
			} else if (type == "w") {
				Log.w(BasicConf.log_tag_all, msg);
				if (tag.length() > 0)
					Log.w(tag, msg);
				else
					Log.w(BasicConf.log_tag_default, msg);
				// 写入系统日志
				if (BasicConf.log_save2file) {
					String msgStr = sdf.format(new Date()) + "-W-" + msg + "\r\n\r\n";
					ByteArrayInputStream stream = new ByteArrayInputStream(msgStr.getBytes());
					UtilFile.saveFileToCompletePath(UtilFile.getSDDir() + "warn_log.txt", stream, true);
				}
			}
			return msg;
		} else
			return "";
	}

	/**
	 * 输出信息
	 * 
	 * @param type
	 *            Log.i,d,(w将输出到系统日志log.txt)
	 * @param str
	 *            输出信息
	 */
	public static String print(String type, String str) {
		return print("", type, str, 2);
	}

	public static String print(String type, String str, int stack) {
		return print("", type, str, stack);
	}

	public static String print(String tag, String type, String str) {
		return print(tag, type, str, 2);
	}

}
