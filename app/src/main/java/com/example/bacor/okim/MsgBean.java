package com.example.bacor.okim;

public class MsgBean {
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SEND = 2;

    private MsgWrapper msgWrapper;
    private int type;

    public MsgBean(MsgWrapper msgWrapper, int type) {
        this.msgWrapper = msgWrapper;
        this.type = type;
    }

    public MsgWrapper getContent() {
        return msgWrapper;
    }

    public void setContent(MsgWrapper msgWrapper) {
        this.msgWrapper = msgWrapper;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
