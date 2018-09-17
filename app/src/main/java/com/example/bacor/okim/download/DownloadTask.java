package com.example.bacor.okim.download;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * String:传入的URL
 * INTEGER:更新的值
 * INTEGER:处理结果
 */
public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    private static final int TYPE_SUCCESS = 100;
    private static final int TYPE_PAUSED = 101;
    private static final int TYPE_CANCELED = 102;
    private static final int TYPE_FAILED = 103;

    private boolean isPaused = false;
    private boolean isCanceled = false;

    private OnDownloadListener listener;

    private int lastProgress = 0;

    public DownloadTask(OnDownloadListener listener){
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        RandomAccessFile randomAccessFile = null;
        File file = null;
        InputStream is = null;

        try {
            long downloadedLength = 0;
            long contentLength;
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            String downloadUrl = strings[0];
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            file = new File(directory+fileName);
            if(file.exists())
                downloadedLength = file.length();
            contentLength = getContentLength(downloadUrl);
            if(contentLength == 0)
                return TYPE_FAILED;
            else if(downloadedLength == contentLength)
                return TYPE_SUCCESS;

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //断点续传
                    .addHeader("RANGE","byte="+downloadedLength+"-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(response != null){
                is = response.body().byteStream();
                //从已下载文件开始写入
                randomAccessFile = new RandomAccessFile(file,"rw");
                randomAccessFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int len=0, total=0;
                while((len = is.read(b)) != -1){
                    //暂停和取消返回做回调处理
                    if(isPaused)
                        return TYPE_PAUSED;
                    else if(isCanceled)
                        return TYPE_CANCELED;
                    else{
                        total += len;
                        randomAccessFile.write(b,0,len);
                        int progress = (int) ((total+downloadedLength)*100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.close();
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if (is != null) {
                    is.close();
                }
                if(randomAccessFile != null)
                    randomAccessFile.close();
                if(isCanceled && file != null)
                    file.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int progress = values[0];
        if(progress > lastProgress)
            listener.onProgress(progress);  //回调更新
        lastProgress = progress;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        switch(integer){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
        }
    }

    private long getContentLength(String downloadUrl) throws IOException {
        long contentLength = 0;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if(response != null){
            if(response.isSuccessful() && response.body() != null) {
                contentLength = response.body().contentLength();
            }
            response.close();
        }
        return contentLength;
    }

    public void setPauseDownload(boolean b){
        this.isPaused = b;
    }

    public void cancelDownload(){
        isCanceled = true;
    }
}
