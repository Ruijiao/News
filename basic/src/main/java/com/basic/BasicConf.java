package com.basic;

import java.util.ArrayList;
import java.util.Map;

/**
 * 配置基类
 *
 * @author Jerry
 */
public abstract class BasicConf {
	// -------------------------------------文件配置---------------------------------------//
	/** sd卡存储文件夹名 */
	public static String file_sdCardDir = "/xhFile";
	/** data存储路径 */
	public static String file_dataDir = "/xh.basic/file";
	/** 文件编码 */
	public static String file_encoding = "HTTP.UTF_8";
	// --------------------------------------日志配置---------------------------------------//
	/** 是否输出日志 */
	public static boolean log_isDebug = true;
	/** 是否将log输出保存到日志 */
	public static boolean log_save2file = false;
	/** logcat默认日志filter */
	public static String log_tag_default = "xh_default";
	/** 网络框架输出log的tag名 */
	public static String log_tag_net = "xh_network";
	/** 网络框架输出log的tag名 */
	public static String log_tag_img = "xh_img";
	/** logcat所有日志filter */
	public static String log_tag_all = "xh_all";
	// --------------------------------------网络配置---------------------------------------//
	/** 超时时间 */
	public static int net_timeout = 20;
	/** 请求图片时需要设置的referer */
	public static String net_imgRefererUrl = "";
	/** 上传图片时将流变为jpg格式，保证压缩质量 */
	public static boolean net_imgUploadJpg = false;
	/** 编码设置 */
	public static String net_encode = "HTTP.UTF_8";
	/**
	 * 域名转换IP配置，请求时直接请求IP，域名会放到header的host中，为空则忽略配置
	 * { "api.xiangha.com":[ //域名
	 * {"ip":"123.123.123.1", //IP
	 * "weight":20 //权重 },
	 * { "ip":"123.123.123.2",//IP
	 * "weight":80 //权重 }]
	 * }
	 */
	public static String net_domain2ipJson = "";
	/** 域名IP绑定关系 */
	public static Map<String, ArrayList<Map<String, String>>> net_domain2ipObj;
	/** 图片上传最小宽 */
	public static int net_imgUploadWidth = 900;
	/** 图片上传最小高 */
	public static int net_imgUploadHeight = 900;
	/** 图片上传理论最大kb数（byte大小，约是实际大小的2倍） */
	public static int net_imgUploadKb = 300;
	// --------------------------------------图片配置---------------------------------------//
	/** 图片展示最小宽 */
	public static int img_showWidth = 900;
	/** 图片展示最小高 */
	public static int img_showHeight = 900;
	/** 图片展示理论最大kb数 */
	public static int img_showKb = 150;
	/** 图片默认cache大小 */
	public static int img_diskCacheSize = 100 * 1024 * 1024;
	/** 默认占位图ID */
	public static int img_placeholderID = 0;
	/** 默认错误占位图ID */
	public static int img_errorID = 0;
	/** 默认错误占位图ID */
	public static String img_defaultPath = "file:///android_asset/i_nopic.png";
	/** 是否使用DNS解析 */
	public static boolean IMAGE_USE_DNS = false;
	/** 默认使用DNS解析策略 */
	public static boolean IMAGE_OPEN_DNS = true;
}
