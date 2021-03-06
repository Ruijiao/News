package config;

import android.app.Activity;
import android.content.Context;

import com.basic.internet.img.UtilLoadImage;

public class LoadImage extends UtilLoadImage {
	public static Context initContext = null;
	private static LoadImage instance = null;

	private LoadImage(Context context){
		super(context);
	}
	
	/**初始化*/
	public static LoadImage init(Context context){
		initContext = context;
		return getInstance();
	}
	
	public static LoadImage getInstance() {
		if (instance == null) {
			instance = new LoadImage(initContext);
		}
		return instance;
	}
	
	public static Builder with(Activity activity){
		return getInstance().getBuilder(activity);
	}
	
	public static Builder with(Context context){
		return getInstance().getBuilder(context);
	}
}
