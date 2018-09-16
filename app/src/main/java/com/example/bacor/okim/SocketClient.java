package com.example.bacor.okim;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * 保存全局socket实例
 * 使用单例模式
 * 返回的单例都有可能是null
 */

public class SocketClient {
    private static final String TAG = SocketClient.class.getSimpleName();

    private static Socket socket;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;

    public static Socket getSingleSocket(){
        if(socket == null){
            synchronized(SocketClient.class){
                if(socket == null) {
                    try {
                        socket = new Socket(Constants.HOST, Constants.PORT);
                        Log.d(TAG, "连接成功");
                        Log.d(TAG, "socket: "+socket.toString());
                    } catch (IOException e) {
                        Log.d(TAG, "连接失败");
                        e.printStackTrace();
                    }
                }
            }
        }
        return socket;
    }

    public static ObjectInputStream getSingleOIS(Socket socket){
        if(ois == null) {
            synchronized(SocketClient.class) {
                if(ois == null) {
                    if (socket != null) {
                        try {
                            ois = new ObjectInputStream(socket.getInputStream());
                            Log.d(TAG, "getSingleOIS: 创建OIS成功");
                        } catch (IOException e) {
                            Log.d(TAG, "getSingleOIS: 创建OIS失败");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return ois;
    }

    public static ObjectOutputStream getSingleOOS(Socket socket){
        if(oos == null) {
            synchronized(SocketClient.class) {
                if(oos == null) {
                    if (socket != null) {
                        try {
                            oos = new ObjectOutputStream(socket.getOutputStream());
                            Log.d(TAG, "getSingleOOS: 创建OOS成功");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "getSingleOOS: 创建OOS成功");
                        }
                    }
                }
            }
        }
        return oos;
    }

    public static boolean closeSocket(){
        boolean result = false;
        if(socket != null){
            if(socket.isClosed()) {
                try {
                    socket.close();
                    oos.close();
                    ois.close();
                    result = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else
            result = true;
        return result;
    }
}
