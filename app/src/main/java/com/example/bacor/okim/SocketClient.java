package com.example.bacor.okim;

import java.io.IOException;
import java.net.Socket;

/**
 * 保存全局socket实例
 * 使用单例模式
 */

public class SocketClient {
    private static Socket socket;
    static{
        try {
            socket = new Socket(Constants.HOST,Constants.PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Socket getInstance(){
        return socket;
    }
}
