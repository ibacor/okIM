package com.example.bacor.okim.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.bacor.okim.MainActivity;
import com.example.bacor.okim.R;

public class DownloadService extends Service{
    private DownloadTask downloadTask;

    private OnDownloadListener listener = new OnDownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1,getNotification("正在下载...",progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载已经完成",-1));
            Toast.makeText(DownloadService.this,"下载完成",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1,getNotification("下载失败了",-1));
            Toast.makeText(DownloadService.this,"下载失败了",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            Toast.makeText(DownloadService.this,"下载暂停",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this,"下载已取消",Toast.LENGTH_LONG).show();
        }
    };
    private DownloadBinder mBinder = new DownloadBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
        builder.setContentIntent(pi);

        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));
        if(progress > 0){
            builder.setContentText(progress + "%");
            builder.setProgress(100,progress,false);
        }
        return builder.build();
    }

    public class DownloadBinder extends Binder{
        public void startDownload(String url){
            if(downloadTask == null){
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(url);
                startForeground(1, getNotification("正在下载...",0));
                Toast.makeText(DownloadService.this,"下载开始",Toast.LENGTH_LONG).show();
            }
        }

        public void pauseDownload(){
            if(downloadTask != null)
                downloadTask.setPauseDownload(true);
        }

        public void cancelDownload(){
            if(downloadTask != null)
                downloadTask.cancelDownload();
        }
    }
}
