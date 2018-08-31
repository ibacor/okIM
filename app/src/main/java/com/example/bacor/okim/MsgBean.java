package com.example.bacor.okim;

public class MsgBean {
    public static final int TYPE_RECEIVED = 1;
    public static final int TYPE_SEND = 2;

    private String content;
    private int type;

    public MsgBean(String content, int type) {
        this.content = content;
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
