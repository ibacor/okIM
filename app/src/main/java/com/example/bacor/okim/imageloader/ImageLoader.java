package com.example.bacor.okim.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.bacor.okim.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    private static final int MESSAGE_POST_RESULT = 1;
    private static final long DISK_CACHE_SIZE = 1024*1024*50;   //50M
    private static final int DISK_CACHE_INDEX = 0;
    private static final int IO_BUFFER_SIZE = 8*1024;
    private static final int TAG_KEY_URI = R.id.imageloader_uri;

    private boolean mIsDiskLruCacheCreated = false;

    //线程池相关
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = 2*CPU_COUNT +1;
    private static final long KEEP_ALIVE = 10L;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable,"ImageLoader#"+mCount.getAndIncrement());
        }
    };
    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,KEEP_ALIVE,
            TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),sThreadFactory);

    //Handler
    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            imageView.setImageBitmap(result.bitmap);
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            if(uri.equals(result.uri))
                imageView.setImageBitmap(result.bitmap);
        }
    };

    //内存缓存和磁盘缓存
    private LruCache<String,Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;

    private ImageResizer mImageResizer = new ImageResizer();
    private Context mContext;

    //获取ImageLoader实例
    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    private ImageLoader(Context context) {
        mContext = context;
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;  //设置为进程最大内存的1/8
        mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight()/1024;    //单位为M
            }
        };
        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        //获取磁盘容量大小
        if(getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //同步加载
    public Bitmap loadBitmap(String uri, int reqWidth, int reqHeight){
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if(bitmap != null)
            return bitmap;

        try {
            //从磁盘中读取
            bitmap = loadBitmapFromDiskCache(uri, reqWidth, reqHeight);
            if(bitmap != null)
                return bitmap;
            //拉取网络图片缓存到本地
            bitmap = loadBitmapFromHttp(uri,reqWidth,reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(bitmap == null && !mIsDiskLruCacheCreated)
            //缓存失败，越过缓存拉取后直接显示
            bitmap = downloadBitmapFromUrl(uri);

        return bitmap;
    }

    //异步加载
    public void bindBitmap(final String uri, final ImageView imageView, final int reqWidth, final int reqHeight){
        imageView.setTag(TAG_KEY_URI,uri);
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if(bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(uri,reqWidth,reqHeight);
                if(bitmap != null){
                    LoaderResult result = new LoaderResult(imageView,uri,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    //拉取图片并直接显示
    private Bitmap downloadBitmapFromUrl(String sUrl) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream bis = null;

        try {
            URL url = new URL(sUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            bis = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(bis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(urlConnection != null)
                urlConnection.disconnect();
            if(bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }

    private Bitmap loadBitmapFromMemCache(String url) {
        String key = hashKeyFromUrl(url);
        return getBitmapFromMemCache(key);
    }

    //内存缓存对缓存的添加和获取操作
    private void addBitmapToMemoryCache(String key, Bitmap bitmap){
        if(getBitmapFromMemCache(key) == null)
            mMemoryCache.put(key,bitmap);
    }

    //磁盘缓存对缓存的添加和获取操作
    //从网络中拉取图片到本地
    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException {
        //判断是否在UI线程执行网络操作
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("load image from UI Thread!");
        }
        if(mDiskLruCache == null)
            return null;

        String key = hashKeyFromUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor != null){
            OutputStream os = editor.newOutputStream(DISK_CACHE_INDEX);
            if(downloadUrlToStream(url,os))
                editor.commit();
            else
                editor.abort();
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException {
        if(Looper.myLooper() == Looper.getMainLooper())
            Log.w(TAG, "load bitmap from disk in UI Thread!");
        if(mDiskLruCache == null)
            return null;

        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if(snapshot != null){
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fd = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fd,reqWidth,reqHeight);
            if(bitmap != null)
                addBitmapToMemoryCache(key,bitmap);
        }
        return bitmap;
    }

    //把网络流转为java IO流
    private boolean downloadUrlToStream(String sUrl, OutputStream os) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            URL url = new URL(sUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            bis = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bos = new BufferedOutputStream(os,IO_BUFFER_SIZE);

            int b;
            while((b=bis.read()) != -1)
                bos.write(b);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(urlConnection != null)
                urlConnection.disconnect();
            if(bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return false;
    }

    //把url转为MD5值，避免URL中的特殊字符
    private String hashKeyFromUrl(String url) {
        String key;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            key = byteToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            key = String.valueOf(url.hashCode());
            e.printStackTrace();
        }
        return key;
    }

    private String byteToHexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if(hex.length() == 1)
                sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    //获取磁盘可用空间
    private long getUsableSpace(File diskCacheDir) {
        //这个版本太低了，一般来说不需要
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            return diskCacheDir.getUsableSpace();
        StatFs statFs = new StatFs(diskCacheDir.getPath());
        return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
    }

    //获取保存文件目录
    private File getDiskCacheDir(Context mContext, String name) {
        String dir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            dir = mContext.getExternalCacheDir().getPath();
        else
            dir = mContext.getCacheDir().getPath();
        return new File(dir + File.separator + name);
    }

    private class LoaderResult {
        ImageView imageView;
        String uri;
        Bitmap bitmap;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap){
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }
}
