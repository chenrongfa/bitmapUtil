package net.bither.util;

import android.graphics.Bitmap;

/**
 * Created by crf on 2017/9/6.
 * company:逸辰
 * qq:952786280
 * feature:
 */

public class Test {
	private static native String compressBitmap(Bitmap bit, int w, int h, int quality, byte[] fileNameBytes,
	                                            boolean optimize);
}
