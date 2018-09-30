package com.example.bacor.okim.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

public class ImageResizer {
    private static final String TAG = "ImageResizer";

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        //不载入资源测量大小
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        //载入资源
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resId,options);
    }

    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        //不载入资源测量大小
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        //载入资源
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    /**
     * 根据bitmap资源大小和需求大小返回缩放比例
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return 缩放比例
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        if(reqWidth<=0 || reqHeight<=0)
            return 1;
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if(height>reqHeight || width>reqWidth){
            int halfHeight = height/2;
            int halfWidth = width/2;
            while((halfHeight/inSampleSize) >= reqHeight && (halfWidth/inSampleSize) >= reqWidth)
                inSampleSize *= 2;
        }
        return inSampleSize;
    }
}
