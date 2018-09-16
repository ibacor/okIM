package com.example.bacor.okim;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ExecutorService threadPool;
    Socket socket;
    private static final String TAG = MainActivity.class.getSimpleName();

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
