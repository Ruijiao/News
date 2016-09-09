package com.basic.tool;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.basic.BasicConf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UtilImage {
	// 获取优化内存的图片模式
	public static Options getBitmapOpt() {
		// 配置bitmap，防止内存溢出
		Options opt = new Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		return opt;
	}

	/**
	 * 根据图片路径得到图片文件的type
	 * @param imgPath
	 * @return
	 */
	public static CompressFormat getImgFormat(String imgPath) {
		Options options = getBitmapOpt();
		options.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeFile(imgPath, options);

		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		}
		return getImgFormat(options);
	}

	public static CompressFormat getImgFormat(Options options) {
		String type = options.outMimeType;
		if (type != null && type.indexOf("png") > -1)
			return CompressFormat.PNG;
		else
			return CompressFormat.JPEG;
	}

	/**
	 * 图片圆角转换
	 * @param res 当前资源类
	 * @param drawable
	 * @param type
	 *            1全圆角，2仅上半部分圆角
	 * @param pixels
	 *            圆角大小
	 * @return
	 */
	public static Bitmap toRoundCorner(Resources res, Bitmap bitmap, int type, int pixels) {
		Bitmap output = null;
		if (pixels == 0)
			return bitmap;
		try {
			int width = bitmap.getWidth(), height = bitmap.getHeight();
			output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, width, height);
			final RectF rectF = new RectF(rect);
			final float roundPx = pixels;
			paint.setAntiAlias(true);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(0xff424242);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			switch (type) {
			// 仅上半部分圆角
			case 2:
				canvas.drawRect(0, height - pixels, width, height, paint);
				break;
			}
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
		} catch (Exception e) {
		} catch (Error e) {
		}
		return output;
	}
	/**
	 * 不论图片大小，对bitmap转圆角
	 * @param bitmap
	 * @return
	 */
	public static Bitmap makeRoundCorner(Bitmap bitmap) {  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        int left = 0, top = 0, right = width, bottom = height;  
        float roundPx = height / 2;  
        if (width > height) {  
            left = (width - height) / 2;  
            top = 0;  
            right = left + height;  
            bottom = height;  
        } else if (height > width) {  
            left = 0;  
            top = (height - width) / 2;  
            right = width;  
            bottom = top + width;  
            roundPx = width / 2;  
        }  
  
        Bitmap output = Bitmap.createBitmap(width, height,  
                Bitmap.Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
        int color = 0xff424242;  
        Paint paint = new Paint();  
        Rect rect = new Rect(left, top, right, bottom);  
        RectF rectF = new RectF(rect);  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        return output;  
    }  

	/**
	 * 将图片文件转换成bitmap
	 * @param imgPath 图片文件路径
	 * @param width 宽度
	 * @param height 高度
	 * @param isThumbnail 是否根据高宽生成缩略图
	 * @param opts
	 * @return
	 */
	public static Bitmap imgPathToBitmap(String imgPath, int width, int height, boolean isThumbnail, Options opts) {
		Bitmap bitmap = null;
		Options options = opts == null ? getBitmapOpt() : opts;
		if ((height == 0 && width == 0) || isThumbnail) {
			options.inJustDecodeBounds = false;
			try {
				bitmap = BitmapFactory.decodeFile(imgPath, options);
			} catch (OutOfMemoryError e) {
			}
		}
		// 需要缩放比例
		else {
			options.inJustDecodeBounds = true;
			try {
				bitmap = BitmapFactory.decodeFile(imgPath, options);
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
			}
			// 计算缩放比
			int h = options.outHeight;
			int w = options.outWidth;
			int be = 1;
			if (width > 0 && w > width) {
				be = w / width;
			}
			if (height > 0 && h > height) {
				int be2 = h / height;
				if (be2 > be)
					be = be2;
			}
			options.inSampleSize = be;
			options.inJustDecodeBounds = false;
			try {

				bitmap = BitmapFactory.decodeFile(imgPath, options);
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
			}
			UtilLog.print(BasicConf.log_tag_img, "d",
					"图片缩放-原图：" + w + "*" + h + "，"
					+ "要求：" + width + "*" + height + "。"
					+ "压缩比例为" + be);
		}
		int degree = getPictureDegree(imgPath);
		// 如果有角度上的旋转,则纠正图片;
		if (degree != 0) {
			bitmap = correctImage(degree, bitmap);
		}
		// 生成固定尺寸的缩略图
		if (isThumbnail) {
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

	/**
	 * bitmap转换为byte[]，用完记得关闭流
	 * @param bitmap
	 * @param format
	 * @param kb 最大允许kb
	 * @return
	 */
	public static byte[] bitmapToByte(Bitmap bitmap, CompressFormat format, int kb) {
		int quality = 100, num = 1;
		byte[] theByte = null;
		// 压缩
		while (bitmap != null && num < 6) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				boolean isOk = bitmap.compress(format, quality, baos);
				if (isOk) {
					theByte = baos.toByteArray();
					UtilLog.print(BasicConf.log_tag_img, "d" , 
							"图片压缩-" + format 
							+ "原图：" + bitmap.getWidth() + "*" + bitmap.getHeight() + "，"
							+ "需要" + kb + "K，"
							+ "质量" + quality + "，"
							+ "结果：" + (theByte.length / 1024) + "K");
					baos.close();
					if (theByte.length / 1024 < kb || kb == 0) {
						break;
					}
				}
			} catch (Exception e) {
				UtilLog.reportError("图片压缩", e);
				break;
			} catch (Error e) {
				quality -= 3;
			}
			quality -= num * 3;
			num++;
		}
		return theByte;
	}

	/**
	 * 将InputStream 转换成 Bitmap，自动关闭了流
	 * 
	 * @param is
	 * @return
	 */
	public static Bitmap inputStreamTobitmap(InputStream is) {
		if (is == null)
			return null;
		Options options = getBitmapOpt();
		Bitmap bitmap = null;
		try {

			bitmap = BitmapFactory.decodeStream(is, null, options);
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		}
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	/**
	 * 等比缩小图片并设置到imageView中，zoom强制等比适应宽或高
	 * 
	 * @param imgView
	 * @param bitmap
	 * @param width
	 * @param height
	 * @param zoom
	 * @return
	 */
	public static LayoutParams setImgViewByWH(ImageView img, Bitmap bitmap, int width, int height, boolean zoom) {
		LayoutParams lp = img.getLayoutParams();
		if (bitmap == null)
			return lp;
		if (height > 0 && width > 0 && zoom) {
			lp.height = height;
			lp.width = width;
		} else if (width > 0 && bitmap.getWidth() > 0) {
			lp.height = bitmap.getHeight() * width / bitmap.getWidth();
			lp.width = width;
		} else if (height > 0 && bitmap.getHeight() > 0) {
			lp.height = height;
			lp.width = bitmap.getWidth() * height / bitmap.getHeight();
		}
		if (height > 0 || width > 0) {
			img.setLayoutParams(lp);
		}
		img.setImageBitmap(bitmap);
		return lp;
	}

	/**
	 * 根据角度值,重新构造一个正向的图片;
	 * @param angle
	 * @param bitmap
	 * @return
	 */
	public static Bitmap correctImage(int angle, Bitmap bitmap) {
		if(bitmap == null) return null;
		Matrix mx = new Matrix();
		mx.postRotate(angle);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, mx, true);
		return resizedBitmap;
	}

	/**
	 * 获取图片的旋转角度
	 * 
	 * @param path 图片路径
	 * @return 返回已经旋转的角度值
	 */
	public static int getPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return degree;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 高斯模糊，一般方向模糊10，迭代度7
	 * 
	 * @param bmp 图片
	 * @param hRadius 水平方向模糊度
	 * @param vRadius 竖直方向模糊度
	 * @param iterations 模糊迭代度
	 * @return
	 */
	public static Bitmap BoxBlurFilter(Bitmap bmp, int hRadius, int vRadius, int iterations) {
		// return bmp;
		try {
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			int[] inPixels = new int[width * height];
			int[] outPixels = new int[width * height];
			Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
			for (int i = 0; i < iterations; i++) {
				blur(inPixels, outPixels, width, height, hRadius);
				blur(outPixels, inPixels, height, width, vRadius);
			}
			blurFractional(inPixels, outPixels, width, height, hRadius);
			blurFractional(outPixels, inPixels, height, width, vRadius);
			if (bitmap == null)
				return bmp;
			bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (Exception e) {
			UtilLog.reportError("图片高斯模糊异常", e);
			return bmp;
		} catch (Error e) {
			return bmp;
		}
	}

	private static void blur(int[] in, int[] out, int width, int height, int radius) {
		int widthMinus1 = width - 1;
		int r = radius;
		int tableSize = 2 * r + 1;
		int divide[] = new int[256 * tableSize];

		for (int i = 0; i < 256 * tableSize; i++)
			divide[i] = i / tableSize;

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -r; i <= r; i++) {
				int inIndexUp = (i < 0) ? 0 : (i > (width - 1)) ? (width - 1) : i;
				int rgb = in[inIndex + inIndexUp];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

				int i1 = x + r + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - r;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	private static void blurFractional(int[] in, int[] out, int width, int height, float radius) {
		radius -= (int) radius;
		float f = 1.0f / (1 + 2 * radius);
		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;

			out[outIndex] = in[0];
			outIndex += height;
			for (int x = 1; x < width - 1; x++) {
				int i = inIndex + x;
				int rgb1 = in[i - 1];
				int rgb2 = in[i];
				int rgb3 = in[i + 1];

				int a1 = (rgb1 >> 24) & 0xff;
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;
				int a2 = (rgb2 >> 24) & 0xff;
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;
				int a3 = (rgb3 >> 24) & 0xff;
				int r3 = (rgb3 >> 16) & 0xff;
				int g3 = (rgb3 >> 8) & 0xff;
				int b3 = rgb3 & 0xff;
				a1 = a2 + (int) ((a1 + a3) * radius);
				r1 = r2 + (int) ((r1 + r3) * radius);
				g1 = g2 + (int) ((g1 + g3) * radius);
				b1 = b2 + (int) ((b1 + b3) * radius);
				a1 *= f;
				r1 *= f;
				g1 *= f;
				b1 *= f;
				out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
				outIndex += height;
			}
			// 莫名的会数组溢出
			try {
				out[outIndex] = in[width - 1];
			} catch (Error error) {
			}
			inIndex += width;
		}
	}
}
