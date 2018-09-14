package com.example.bacor.okim;

/**
 * 接收消息监听
 */

public interface IReceiveListener {
    void onReceive(MsgWrapper msgWrapper);
}
