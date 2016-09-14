package config;

import android.content.Context;

import com.basic.BasicConf;

/**
 * 基础包配置类
 * @author Jerry
 */
public class XHConf extends BasicConf {
	/**
	 * 程序初始化时调用，修改框架配置
	 */
	public static void init(Context context){
		//文件配置
		file_sdCardDir="/xiangha";
		file_dataDir="/com.xiangha/file";		//此路径最好用应用包名做文件夹
//		file_encoding=HTTP.UTF_8;
				
		//日志配置
		log_isDebug = true;
		log_save2file=false;
		log_tag_all="xh_all";
		log_tag_default="xh_default";
		log_tag_img="xh_img";
		log_tag_net="xh_network";
		
		//网络配置
		net_timeout=20;
		net_imgRefererUrl="www.xiangha.com";
		net_imgUploadJpg=true;
//		net_encode=HTTP.UTF_8;
		net_imgUploadHeight=net_imgUploadWidth=900;
		net_imgUploadKb=300;

		//图片占位图
//		img_errorID = R.drawable.ic_launcher;
//		img_placeholderID = R.drawable.ic_launcher;
		img_defaultPath = "file:///android_asset/i_nopic.png";
	}
}
