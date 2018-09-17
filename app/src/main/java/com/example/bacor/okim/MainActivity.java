package com.example.bacor.okim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bacor.okim.download.DownloadService;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    ExecutorService threadPool;
    Socket socket;

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPool = Executors.newCachedThreadPool();

        Button button = (Button) findViewById(R.id.main_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,MsgMainActivity.class);
                startActivity(intent);
            }
        });

        Button login = findViewById(R.id.main_btn_login);
        Button disconnect = findViewById(R.id.main_btn_disconnect);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etLogin = findViewById(R.id.main_et_login);
                EditText etFriendId = findViewById(R.id.main_et_friend);
                String friendId = etFriendId.getText().toString();
                String userId = etLogin.getText().toString();
                Friend friend = new Friend();
                friend.setUserId(userId);
                friend.setFriend(friendId);

                login();
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(socket!=null && !socket.isClosed())
                                socket.close();
                            socket = null;
                            Log.d(TAG,"logout success");
                        } catch (IOException e) {
                            Log.d(TAG, "logout failed");
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        initDownLoad();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"您已拒绝权限",Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void initDownLoad(){
        Intent intent = new Intent(this,DownloadService.class);
        startService(intent);
        bindService(intent,connection,BIND_AUTO_CREATE);
        //检查读写外部存储权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        Button start = findViewById(R.id.main_btn_start_download);
        Button pause = findViewById(R.id.main_btn_pause_download);
        Button cancel = findViewById(R.id.main_btn_cancel_download);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadBinder.startDownload("https://raw.githubusercontent.com/guolindev/eclipse/master/eclipse-inst-win64.exe");
            }
        });
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadBinder.pauseDownload();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadBinder.cancelDownload();
            }
        });
    }

    private void login(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket = SocketClient.getSingleSocket();
                Message msg = new Message();
                if(socket != null) {
                    try {
                        ObjectOutputStream oos = SocketClient.getSingleOOS(socket);
                        MsgWrapper msgWrapper = new MsgWrapper(Constants.LOGIN, new Friend().getUserId(), "", "");
                        oos.writeObject(msgWrapper);
                        oos.flush();
                        msg.what = Constants.SUCCEED;
                    } catch (IOException e) {
                        e.printStackTrace();
                        msg.what = Constants.FAILED;
                    }
                }else
                    msg.what = Constants.FAILED;
                handler.sendMessage(msg);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case Constants.SUCCEED:
                    Log.d(TAG, "handleMessage: login succeed");
                    break;
                case Constants.FAILED:
                    Log.d(TAG, "handleMessage: login failed");
                    break;
            }
        }
    };
}
