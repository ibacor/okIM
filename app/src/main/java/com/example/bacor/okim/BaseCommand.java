package com.example.bacor.okim;

import java.io.Serializable;

/**
 * 与服务器通信发送指令，通过flag识别指令类别
 */

public class BaseCommand implements Serializable {
    protected String flag;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
