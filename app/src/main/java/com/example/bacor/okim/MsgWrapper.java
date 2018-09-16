package com.example.bacor.okim;

/**
 * 封装发送的消息，消息对象直接抽象成Object，在解析时才向下转换
 */

public class MsgWrapper extends BaseCommand{
    //发送方id
    protected String userId;
    //接收方id
    protected String receiver;
    //消息对象
    protected Object msg;

    public MsgWrapper(String flag, String userId, String receiver, Object msg) {
        this.flag = flag;
        this.userId = userId;
        this.receiver = receiver;
        this.msg = msg;
    }

    public String getUserId() {
        return userId;
    }

    public String getReceiver() {
        return receiver;
    }

    public Object getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "MsgWrapper{" +
                "userId='" + userId + '\'' +
                ", receiver='" + receiver + '\'' +
                ", msg=" + msg +
                ", flag='" + flag + '\'' +
                '}';
    }
}
