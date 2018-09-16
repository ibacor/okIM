package com.example.bacor.okim;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * 封装消息接收功能
 * 由于socket获取输入流会阻塞，所以另开线程
 */

public class MsgReceive {
    private static final String TAG = MsgReceive.class.getSimpleName();

    private IReceiveListener mListener;

    public void setOnReceiveListener(IReceiveListener listener){
        mListener = listener;
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case Constants.SUCCEED:
                    if(msg.getData()!=null && msg.getData().getSerializable("receive_msg")!=null) {
                        MsgWrapper receiveMsg = (MsgWrapper) msg.getData().getSerializable("receive_msg");
                        mListener.onReceive(receiveMsg);
                    }
                    break;
            }
        }
    };

    public void startReceive(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket;
                ObjectInputStream ois;

                try {
                    socket = SocketClient.getSingleSocket();
                    ois = SocketClient.getSingleOIS(socket);
                    while(true) {
                        MsgWrapper receiveMsg = (MsgWrapper) ois.readObject();
                        Log.d(TAG, "received: " + receiveMsg.toString());
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("receive_msg",receiveMsg);
                        msg.setData(bundle);
                        msg.what = Constants.SUCCEED;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
