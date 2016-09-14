package config.tools;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by xiangha on 2016/9/5.
 */

public class Tools {

    /**
     * @param context 上下文
     * @param returnObj 需要弹出的内容,如果是空的内容,就不弹.
     */
    public static void showToast(Context context, String returnObj) {
        if (context != null && returnObj != "" && returnObj != null && !"[]".equals(returnObj)) {
            Toast.makeText(context, returnObj, Toast.LENGTH_SHORT).show();
        }
    }
}
