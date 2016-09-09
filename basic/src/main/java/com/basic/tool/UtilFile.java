/**
 * @author Jerry
 * 2012-12-30 上午10:17:48
 * Copyright: Copyright (c) xiangha.com 2011
 */
package com.basic.tool;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.basic.BasicConf;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;


public class UtilFile {

	/**
	 * 在SD卡上存文件
	 * 
	 * @param completePath : 完整路径
	 * @param str
	 * @param append 是否在文件后面增加字符
	 * @return 成功file，失败null
	 */
	public static File saveFileToCompletePath(String completePath, String str, boolean append) {
		File file = new File(completePath);
		FileOutputStream fileOutputStream = null;
		try {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			fileOutputStream = new FileOutputStream(file, append);
			fileOutputStream.write(str.getBytes());
			fileOutputStream.flush();
			fileOutputStream.close();
		} catch (Exception e) {
			// Log.e("test", e.getMessage());
			return null;
		}
		return file;
	}

	/**
	 * 在SD卡上存文件
	 * @param completePath : 完整路径
	 * @param is
	 * @return 成功file，失败null
	 */
	public static File saveFileToCompletePath(String completePath, InputStream is, boolean append) {
		File file = new File(completePath);
		File parentFile = file.getParentFile();
		if (!parentFile.exists())
			parentFile.mkdirs();
		if (is != null) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file, append);
				byte[] b = new byte[1024];
				int len = -1;
				while ((len = is.read(b)) != -1) {
					fos.write(b, 0, len);
				}
				fos.close();
				is.close();
				return file;
			} catch (Exception e) {
				e.printStackTrace();
				// LogManager.reportError("写sd文件异常",e);
				return null;
			}
		} else
			return null;
	}

	/**
	 * 保存图片到sd卡
	 * @param bitmap
	 * @param completePath : 完整路径
	 */
	public static void saveImgToCompletePath(Bitmap bitmap, String completePath, CompressFormat format) {
		File file = new File(completePath);
		File parentFile = file.getParentFile();
		if (!parentFile.exists())
			parentFile.mkdirs();
		if (bitmap != null) {
			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(format, 100, bos);
			} catch (Exception e) {
				byte[] theByte = UtilImage.bitmapToByte(bitmap, format, 0);
				if (theByte != null) {
					saveFileToCompletePath(getSDDir() + file.getAbsolutePath(), new ByteArrayInputStream(theByte),
							false);
				}
			}
		}
	}

	/**
	 * 读取文件
	 * @param completePath ：文件的完整的路径
	 * @return
	 */
	public static String readFile(String completePath) {
		FileInputStream fi;
		StringBuilder str = new StringBuilder();
		try {
			fi = new FileInputStream(completePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(fi));
			String readString = "";
			while ((readString = br.readLine()) != null) {
				str.append(readString + "\r\n");
			}
			br.close();
			fi.close();
		} catch (Exception e) {
			return "";
		}
		return str.toString();
	}

	/**
	 * 读取SD卡上文件
	 * @param completePath ：文件的完整的路径
	 * @return
	 */
	public static InputStream loadFile(String completePath) {
		try {
			InputStream fi = new FileInputStream(completePath);
			return fi;
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * inputStream转byte[]
	 * @return byte[]
	 */
	public static byte[] inputStream2Byte(InputStream inStream) {
		if (inStream == null)
			return null;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		try {
			while ((len = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, len);
			}
			outStream.close();
			inStream.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return outStream.toByteArray();
	}

	/**
	 * 删除SD卡上时间较早的文件
	 * @param completePath
	 * @param keep 文件夹内只保留
	 *            (keep~keep*2)个文件
	 */
	public static void delDirectoryOrFile(String completePath, int keep) {
		File file = new File(completePath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length - keep * 2 > 0) {
				if (keep > 0) {
					System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
					try {
						Arrays.sort(files, new Comparator<Object>() {
							@Override
							public int compare(Object object1, Object object2) {
								File file1 = (File) object1;
								File file2 = (File) object2;
								long result = file1.lastModified() - file2.lastModified();
								if (result < 0) {
									return -1;
								} else if (result > 0) {
									return 1;
								} else {
									return 0;
								}
							}
						});
					} catch (Exception e) {
						UtilLog.reportError("文件排序错误", e);
					}
				}
				for (int i = 0; i < files.length - keep; i++) {
					files[i].delete();
				}
			}
		} else if (file.isFile()) {
			file.delete();
		}
	}

	/**
	 * 删除SD卡上的文件夹《包括文件夹下的文件》或者文件
	 * @param completePath
	 */
	public static void delDirectoryOrFile(String completePath) {
		delDirectoryOrFile(completePath, 0);
	}

	/**
	 * 文件在是否在hour小时内更新过
	 * @param completePath ： 绝对路径
	 * @param minute 分钟数，为-1只判断是否存在
	 * @return 是：返回文件，否：返回空
	 */
	public static File ifFileModifyByCompletePath(String completePath, int minute) {
		File file = new File(completePath);
		if (!file.exists())
			return null;
		Date date = new Date();
		long fileModify = file.lastModified();
		if (minute == -1)
			return file;
		return fileModify + minute * 60000 - date.getTime() > 0 ? file : null;
	}

	/**
	 * 删除xml元素
	 * @param context
	 * @param key
	 */
	public static void delShared(Context context, String name, String key) {
		SharedPreferences mShared = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		Editor editor = mShared.edit();
		if (key.equals(""))
			editor.clear();
		else
			editor.remove(key);
		editor.commit();
	}

	/**
	 * 读取xml元素
	 * @param context
	 * @param name 文件名，必须在本类的子类中创建静态变量表示文件名
	 * @param key 名
	 * @return
	 */
	public static Object loadShared(Context context, String name, String key) {
		if (context != null && name != null) {
			SharedPreferences mShared = context.getSharedPreferences(name, Context.MODE_PRIVATE);
			if (key.length() == 0)
				return mShared.getAll();
			return mShared.getString(key, "");
		}
		return "";
	}

	/**
	 * 存入xml元素
	 * @param context
	 * @param key
	 * @return
	 */
	public static void saveShared(Context context, String name, String key, String value) {
		SharedPreferences mShared = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		Editor editor = mShared.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * 存为xml
	 * @param context
	 * @param map
	 */
	public static void saveShared(Context context, String name, Map<String, String> map) {
		SharedPreferences mShared = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		Editor editor = mShared.edit();
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();
			editor.putString(entry.getKey(), entry.getValue());
		}
		editor.commit();
	}

	/**
	 * 从assets 文件夹中获取文件并读取数据
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String getFromAssets(Context context, String fileName) {
		String result = "";
		try {
			InputStream in = context.getResources().getAssets().open(fileName);
			// 获取文件的字节数
			int lenght = in.available();
			// 创建byte数组
			byte[] buffer = new byte[lenght];
			// 将文件中的数据读到byte数组中
			in.read(buffer);
			result = EncodingUtils.getString(buffer, BasicConf.file_encoding);
		} catch (Exception e) {
			return "";
		}
		return result;
	}

	/**
	 * 获取文件大小
	 * @param path
	 * @return
	 */
	public static long getFileSize(String path) {
		File file = new File(path);
		long size = 0;
		FileInputStream fis = null;
		try {
			if (file.exists()) {
				fis = new FileInputStream(file);
				size = fis.available();
			} else {
				file.createNewFile();
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	/**
	 * 获取文件后缀
	 * @param fileName
	 * @return
	 */
	public static String getFileSuffix(String fileName) {
		if (fileName == null || fileName.equals(""))
			return "";
		String[] str = fileName.split("\\.");
		if (str.length == 0)
			return "";
		int index = str.length > 0 ? str.length - 1 : 0;
		return str[index];
	}

	/** 从缓存中获取文件流 */
	public static InputStream getFromMyDiskCache(String url, String type) {
		String name = UtilString.toMD5(url, false);
		InputStream is = UtilFile.loadFile(UtilFile.getSDDir() + type + "/" + name);
		return is;
	}

	/** 将文件流存入缓存 */
	public static File saveToMyDiskCache(String url, byte[] bytes, String type) {
		String name = UtilString.toMD5(url, false);
		String completePath = UtilFile.getSDDir() + type + "/" + name;
		return saveFileToCompletePath(completePath, new ByteArrayInputStream(bytes), false);
	}

	/** 获取SD卡路径 */
	public static String getSDDir() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + BasicConf.file_sdCardDir + "/";
	}

	/** 获取App data路径 */
	public static String getDataDir() {
		return Environment.getDataDirectory() + "/data" + BasicConf.file_dataDir + "/";
	}
}
