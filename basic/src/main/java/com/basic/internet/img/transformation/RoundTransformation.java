package com.basic.internet.img.transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;


/**
 * @ClassName RoundTransformation
 * @ClassDesc 
 * @author MrTrying
 * @createTime 2016年5月17日 上午10:05:04
 * @version
 *
 */
public class RoundTransformation extends BitmapTransformation {
	private float radius = 0f;

	public RoundTransformation(Context context) {
		this(context, 4);
	}

	public RoundTransformation(Context context, int px) {
		super(context);
		this.radius = px;
	}

	@Override
	protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
		return roundCrop(pool, toTransform);
	}

	private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
		Bitmap output = null;
		if (source == null)
			return null;
		if (radius == 0)
			return source;
		try {
			int width = source.getWidth(), height = source.getHeight();
			output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, width, height);
			final RectF rectF = new RectF(rect);
			final float roundPx = radius;
			paint.setAntiAlias(true);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(0xff424242);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(source, rect, rect, paint);
		} catch (Exception e) {
		} catch (Error e) {
		}
		return output;
	}

	@Override
	public String getId() {
		return getClass().getName() + Math.round(radius);
	}

}
