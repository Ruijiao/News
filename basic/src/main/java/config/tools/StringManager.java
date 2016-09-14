package config.tools;

import com.basic.tool.UtilString;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringManager extends UtilString {
    //固定URL基础配置
    public final static String defaultDomain = ".xiangha.com";
    public final static String apiTitle = "http://api";
    public final static String appWebTitle = "http://appweb";
    public final static String wwwTitle = "http://www";
    public final static String mmTitle = "http://mm";
    //当前域名
    public static String domain = defaultDomain;
    //API请求地址
    public static String apiUrl = apiTitle + defaultDomain + "/";
    //app网页地址
    public static String appWebUrl = appWebTitle + defaultDomain + "/";
    //PC主网页地址
    public static String wwwUrl = wwwTitle + defaultDomain + "/";
    //手机管理平台地址
    public static String mmUrl = mmTitle + defaultDomain + "/";


    //第三方下载链接（应用宝）
    public final static String third_downLoadUrl = "http://a.app.qq.com/o/simple.jsp?pkgname=com.xiangha";


    /**
     * 将字符串转成MD5值
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
