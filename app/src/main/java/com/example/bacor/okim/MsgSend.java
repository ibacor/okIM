package com.example.bacor.okim;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 封装消息发送功能
 * 为避免阻塞UI线程，这里也另开线程
 */

public class MsgSend {
    private static final String TAG = MsgSend.class.getSimpleName();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case Constants.FAILED:
                    Log.e(TAG, "handleMessage: send message failed");
                    break;
            }
        }
    };

    public void send(final MsgWrapper msgWrapper){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket;
                ObjectOutputStream oos;
                try {
                    socket = SocketClient.getInstance();
                    oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(msgWrapper);
                    oos.flush();
                } catch (IOException e) {
                    //发送失败
                    Message msg = new Message();
                    msg.what = Constants.FAILED;
                    handler.sendMessage(msg);

                    e.printStackTrace();
                }
            }
        }).start();
    }
}
