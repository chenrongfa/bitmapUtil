package net.bither.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * JNI图片压缩工具类
 * 终极压缩(本地结合) 内存波动较小而且 ,压缩效果好
 */
public class BitmapUtil {

	private static int DEFAULT_QUALITY = 95;

	/**
	 * @Description: JNI基本压缩
	 * @param bit
	 *            bitmap对象
	 * @param fileName
	 *            指定保存目录名
	 * @param optimize
	 *            是否采用哈弗曼表数据计算 品质相差5-10倍
	 * @author XiaoSai
	 * @date 2016年3月23日 下午6:32:49
	 * @version V1.0.0
	 */
	public static void compressBitmap(Bitmap bit, String fileName, boolean optimize) {
		saveBitmap(bit, DEFAULT_QUALITY, fileName, optimize);
	}

	/**
	 * @Description: 通过JNI图片压缩把Bitmap保存到指定目录
	 * @param image
	 *            bitmap对象
	 * @param filePath
	 *            要保存的指定目录
	 * @author XiaoSai
	 * @date 2016年3月23日 下午6:28:15
	 * @version V1.0.0
	 */
	public static void compressBitmap(Bitmap image, String filePath) {
		// 最大图片大小 150KB
		int maxSize = 150;
		// 获取尺寸压缩倍数
		int ratio = BitmapUtil.getRatioSize(image.getWidth(),image.getHeight());
		// 压缩Bitmap到对应尺寸
		Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio,image.getHeight() / ratio,Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
		canvas.drawBitmap(image,null,rect,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		result.compress(Bitmap.CompressFormat.JPEG, options, baos);
		// 循环判断如果压缩后图片是否大于100kb,大于继续压缩
		while (baos.toByteArray().length / 1024 > maxSize) {
			// 重置baos即清空baos
			baos.reset();
			// 每次都减少10
			options -= 10;
			// 这里压缩options%，把压缩后的数据存放到baos中
			result.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
		// JNI保存图片到SD卡 这个关键
		BitmapUtil.saveBitmap(result, options, filePath, true);
		// 释放Bitmap
		if (!result.isRecycled()) {
			result.recycle();
		}
	}

	/**
	 *  普通质量压缩(效果还行 但是,内存不减) 貌似  占用的内存很大
	 * @param imageFile
	 * @param filePath
	 */
	public static Bitmap compressBitmapByQuality(String imageFile, String filePath) {
	return 	compressBitmapByQuality(imageFile,filePath, 150);
	}

	/**
	 *
	 * @param imageFile
	 * @param filePath
	 * @param maxSize
	 *
	 */
	private static Bitmap compressBitmapByQuality(String imageFile,String filePath,int maxSize) {
		Bitmap image= BitmapUtil.getBitmapFromFile(imageFile,false);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		image.compress(Bitmap.CompressFormat.JPEG, options, baos);
		// 循环判断如果压缩后图片是否大于100kb,大于继续压缩
		while (baos.toByteArray().length / 1024 > maxSize&&options>10) {
			// 重置baos即清空baos
			baos.reset();
			// 每次都减少10
			options -= 10;
			// 这里压缩options%，把压缩后的数据存放到baos中
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
		FileOutputStream fos=null;
		// 传地址 就保存 没有 略过
		if(filePath!=null){
			try {
				fos = new FileOutputStream(filePath);
				fos.write(baos.toByteArray());
				fos.flush();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					if (fos!=null)
						fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}}
		ByteArrayInputStream bis=new ByteArrayInputStream(baos.toByteArray());
		// 释放Bitmap
		if (!image.isRecycled()) {
			image.recycle();
		}
		return BitmapFactory.decodeStream(bis);

	}

	/***
	 *  先尺寸  压缩 然后 判断合适的质量 ,在进行 本地压缩
	 * @param imageFile
	 * @param filePath
	 */
	public static void compressBitmapBySize(String imageFile, String filePath) {
		// 最大图片大小 150KB
		int maxSize = 150;
		// 获取尺寸压缩倍数
		Bitmap image= getBitmapFromFile(imageFile,true);
		int ratio = BitmapUtil.getRatioSize(image.getWidth(),image.getHeight());
		// 压缩Bitmap到对应尺寸
		Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio,image.getHeight() / ratio,Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
		canvas.drawBitmap(image,null,rect,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		result.compress(Bitmap.CompressFormat.JPEG, options, baos);
		// 循环判断如果压缩后图片是否大于100kb,大于继续压缩
		while (baos.toByteArray().length / 1024 > maxSize &&options>10) {
			// 重置baos即清空baos
			baos.reset();
			// 每次都减少10
			options -= 10;
			// 这里压缩options%，把压缩后的数据存放到baos中
			result.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
		// JNI保存图片到SD卡 这个关键
		BitmapUtil.saveBitmap(result, options, filePath, true);
		// 释放Bitmap
		if (!result.isRecycled()) {
			result.recycle();
		}
		if (!image.isRecycled()) {
			image.recycle();
		}
	}

	/**
	 *  普通尺寸 压缩
	 * @param imageFile
	 * @param filePath
	 */
	public static Bitmap compressBySizeOfOrdinary(String imageFile, String filePath){
		Bitmap image= getBitmapFromFile(imageFile,false);
		int ratio = BitmapUtil.getRatioSize(image.getWidth(),image.getHeight());
		// 压缩Bitmap到对应尺寸
		Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio,image.getHeight() / ratio,Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
		canvas.drawBitmap(image,null,rect,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		result.compress(Bitmap.CompressFormat.JPEG,100,baos);
		FileOutputStream fos=null;
		// 传地址 就保存 没有 略过
		if(filePath!=null){
		try {

			fos = new FileOutputStream(filePath);
			fos.write(baos.toByteArray());
			fos.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (fos!=null)
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}}
		ByteArrayInputStream bis=new ByteArrayInputStream(baos.toByteArray());
		//资源回收
		if(result.isRecycled()){
			result.recycle();
		}//资源回收
		if(image.isRecycled()){
			image.recycle();
		}
	  return 	BitmapFactory.decodeStream(bis);
	}

	/**
	 *  普通采样压缩
	 * @param imageFile
	 * @param filePath
	 * @return
	 */
	public static Bitmap compressBySimpleOfOrdinary(ImageView imageView,String imageFile, String filePath,boolean isAdapter){
		Bitmap image= getBitmapFromFile(imageFile,false);
		int ratio=1;
		//是否适配imageView
		if (!isAdapter)
		 ratio = BitmapUtil.getRatioSize(image.getWidth(),image.getHeight());
		else{
			if (imageView!=null)
				ratio=BitmapUtil.getRatioSizeForImageView(imageView,image);
		}
		// 压缩Bitmap到对应尺寸
		ByteArrayOutputStream bos=new ByteArrayOutputStream();

		image.compress(Bitmap.CompressFormat.JPEG,100,bos);
		if( bos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			bos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, bos);//这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream bis=new ByteArrayInputStream(bos.toByteArray());

		BitmapFactory.Options opt=new BitmapFactory.Options();
		opt.inJustDecodeBounds=false;
		opt.inSampleSize=ratio;
		Bitmap result = BitmapFactory.decodeStream(bis, null, opt);
		bos.reset();
		result.compress(Bitmap.CompressFormat.JPEG,100,bos);
		FileOutputStream fos=null;
		// 传地址 就保存 没有 略过
		if(filePath!=null){
		try {

			fos = new FileOutputStream(filePath);
			fos.write(bos.toByteArray());
			fos.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (fos!=null)
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}}


		//资源回收
		if(image.isRecycled()){
			image.recycle();
		}
	  return result;
	}

	/**
	 *  普通尺寸 压缩 适配 imageView大小 (放大有马赛)
	 * @param imageFile
	 * @param filePath
	 *
	 */
	public static Bitmap compressBySizeOfOrdinaryFim(ImageView imageView,String imageFile, String filePath){
		Bitmap image= getBitmapFromFile(imageFile,false);
		int ratio = BitmapUtil.getRatioSizeForImageView(imageView,image);
		// 压缩Bitmap到对应尺寸
		Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio,image.getHeight() / ratio,Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
		canvas.drawBitmap(image,null,rect,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		result.compress(Bitmap.CompressFormat.JPEG,100,baos);
		FileOutputStream fos=null;
		// 传地址 就保存 没有 略过
		if(filePath!=null){
		try {

			fos = new FileOutputStream(filePath);
			fos.write(baos.toByteArray());
			fos.flush();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (fos!=null)
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}}
		ByteArrayInputStream bis=new ByteArrayInputStream(baos.toByteArray());

		//资源回收
		if(result.isRecycled()){
			result.recycle();
		}
		if(image.isRecycled()){
			image.recycle();
		}
	  return 	BitmapFactory.decodeStream(bis);
	}

	/**
	 *
	 *  尺寸 本地,
	 *  如果 还进行质量 判定 会有 马赛克
	 *  调试 quality 值
	 * @param imageView
	 * @param imageFile
	 * @param filePath
	 * 内存波动 5-12-6
	 */

	public static void compressBitmapForImageviewSize(ImageView imageView,String imageFile, String filePath) {

		// 获取尺寸压缩倍数
		Bitmap image= getBitmapFromFile(imageFile,true);
		int ratio = BitmapUtil.getRatioSizeForImageView(imageView,image);
		// 压缩Bitmap到对应尺寸
		Bitmap result = Bitmap.createBitmap(image.getWidth() / ratio,image.getHeight() / ratio,Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Rect rect = new Rect(0, 0, image.getWidth() / ratio, image.getHeight() / ratio);
		canvas.drawBitmap(image,null,rect,null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;

		// JNI保存图片到SD卡 这个关键
		BitmapUtil.saveBitmap(result, options, filePath, false);
		// 释放Bitmap
		if (!result.isRecycled()) {
			result.recycle();
		}
		if (!image.isRecycled()) {
			image.recycle();
		}
	}

	/**
	 *  采样 本地
	 * @param imageView
	 * @param imageFile
	 * @param filePath
	 * 比上一个波动大
	 */
	public static void compressForImageviewSize(ImageView imageView,String imageFile, String filePath) {

		// 获取尺寸压缩倍数
		Bitmap image= getBitmapFromFile(imageFile,true);
		int ratio = BitmapUtil.getRatioSizeForImageView(imageView,image);
		// 压缩Bitmap到对应尺寸
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG,100,baos);
		ByteArrayInputStream bis=new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options opts=new BitmapFactory.Options();
		opts.inSampleSize=ratio;
		opts.inJustDecodeBounds=false;
		Bitmap bitmap = BitmapFactory.decodeStream(bis, null, opts);
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;

		// JNI保存图片到SD卡 这个关键
		BitmapUtil.saveBitmap(bitmap, options, filePath, false);
		// 释放Bitmap
		if (!bitmap.isRecycled()) {
			bitmap.recycle();
		}
		if (!image.isRecycled()) {
			image.recycle();
		}
	}

	/**
	 * @Description: 通过JNI图片压缩把Bitmap保存到指定目录
	 * @param curFilePath
	 *            当前图片文件地址
	 * @param targetFilePath
	 *            要保存的图片文件地址
	 * @author XiaoSai
	 * @date 2016年9月28日 下午17:43:15
	 * @version V1.0.0
	 *  内存 不减 ,,,,,但是效果好 适合 图片 压缩 上传
	 */
	public static void compressBitmap(String curFilePath, String targetFilePath) {
		// 最大图片大小 150KB
		int maxSize = 150;
		//根据地址获取bitmap
		Bitmap result = getBitmapFromFile(curFilePath,true);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int quality = 100;
		result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		// 循环判断如果压缩后图片是否大于100kb,大于继续压缩 并且 quality 大于0
		while (baos.toByteArray().length / 1024 > maxSize&&quality>10) {
			// 重置baos即清空baos
			baos.reset();
			// 每次都减少10
			quality -= 10;
			// 这里压缩quality，把压缩后的数据存放到baos中(发现 png 图片背景为白色时, 压缩出来的是黑色)
			result.compress(Bitmap.CompressFormat.JPEG, quality, baos);
		}
		// JNI保存图片到SD卡 这个关键
		BitmapUtil.saveBitmap(result, quality, targetFilePath, true);
		// 释放Bitmap
		if (!result.isRecycled()) {
			result.recycle();
		}

	}

	/**
	 * 计算缩放比
	 * @param bitWidth 当前图片宽度
	 * @param bitHeight 当前图片高度
	 * @return int 缩放比
	 * @author XiaoSai
	 * @date 2016年3月21日 下午3:03:38
	 * @version V1.0.0
	 */
	public static int getRatioSize(int bitWidth, int bitHeight) {
		// 图片最大分辨率
		int imageHeight = 1280;
		int imageWidth = 960;
		// 缩放比
		int ratio = 1;
		// 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		if (bitWidth > bitHeight && bitWidth > imageWidth) {
			// 如果图片宽度比高度大,以宽度为基准
			ratio = bitWidth / imageWidth;
		} else if (bitWidth < bitHeight && bitHeight > imageHeight) {
			// 如果图片高度比宽度大，以高度为基准
			ratio = bitHeight / imageHeight;
		}
		// 最小比率为1
		if (ratio <= 0)
			ratio = 1;
		return ratio;
	}

	/**
	 *  适配 Imageview 的大小
	 * @param imageView
	 * @param bitmap
	 * @return
	 */
    public static int getRatioSizeForImageView(ImageView imageView,Bitmap bitmap) {
		// 图片最大分辨率
		int imageHeight = imageView.getHeight();
		int imageWidth = imageView.getWidth();
	    int bitWidth=bitmap.getWidth();
	    int bitHeight=bitmap.getHeight();
		// 缩放比
		int ratio = 1;
		// 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		if (bitWidth > bitHeight && bitWidth > imageWidth) {
			// 如果图片宽度比高度大,以宽度为基准
			ratio = bitWidth / imageWidth;
		} else if (bitWidth < bitHeight && bitHeight > imageHeight) {
			// 如果图片高度比宽度大，以高度为基准
			ratio = bitHeight / imageHeight;
		}
		// 最小比率为1
		if (ratio <= 0)
			ratio = 1;
	    Log.e("tt", "getRatioSizeForImageView: " +ratio);
		return ratio;
	}


	/**
	 *  通过文件路径读获取Bitmap防止OOM以及解决图片旋转问题
	 * @param filePath
	 * @param isNative 是否使用 终极压缩方法
	 * @return
	 */
	public static Bitmap getBitmapFromFile(String filePath,boolean isNative){
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		newOpts.inJustDecodeBounds = true;//只读边,不读内容
		BitmapFactory.decodeFile(filePath, newOpts);
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 获取尺寸压缩倍数
		if(isNative)
		newOpts.inSampleSize = BitmapUtil.getRatioSize(w,h);
		else
			newOpts.inSampleSize=1;

		newOpts.inJustDecodeBounds = false;//读取所有内容
		newOpts.inDither = false;
		newOpts.inPurgeable=true;
		newOpts.inInputShareable=true;
		newOpts.inTempStorage = new byte[32 * 1024];
		Bitmap bitmap = null;
		File file = new File(filePath);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if(fs!=null){
				bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(),null,newOpts);
				//旋转图片
				int photoDegree = readPictureDegree(filePath);
				if(photoDegree != 0){
					Matrix matrix = new Matrix();
					matrix.postRotate(photoDegree);
					// 创建新的图片
					bitmap = Bitmap.createBitmap(bitmap, 0, 0,
							bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(fs!=null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	/**
	 *
	 * 读取图片属性：旋转的角度
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */

	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 调用native方法
	 * @Description:函数描述
	 * @param bit
	 * @param quality
	 * @param fileName
	 * @param optimize
	 * @author XiaoSai
	 * @date 2016年3月23日 下午6:36:46
	 * @version V1.0.0
	 */
	private static void saveBitmap(Bitmap bit, int quality, String fileName, boolean optimize) {
		compressBitmapWidthNative(bit, bit.getWidth(), bit.getHeight(), quality, fileName.getBytes(), optimize);
	}

	/**
	 *
	 * @Description:函数描述
	 * @param bit
	 * @param w
	 * @param h
	 * @param quality
	 * @param fileNameBytes
	 * @param optimize
	 * @return
	 * @author XiaoSai
	 * @date 2016年3月23日 下午6:35:53
	 * @version V1.0.0
	 */
	private static native String compressBitmapWidthNative(Bitmap bit, int w, int h, int quality, byte[] fileNameBytes,
												boolean optimize);
	/**
	 * 加载lib下两个so文件
	 */
	static {
		System.loadLibrary("jpegbither");
		System.loadLibrary("bitherjni");
	}

}