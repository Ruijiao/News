package config.tools;

import android.os.Environment;

import com.basic.BasicConf;
import com.basic.tool.UtilFile;

/**
 * Created by xiangha on 2016/9/5.
 */

public class FileManager extends UtilFile {
    public static final String save_cache = "cache";
    public static String getSDDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + BasicConf.file_sdCardDir + "/";
    }
}
