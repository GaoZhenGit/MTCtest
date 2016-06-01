package com.gz.mtc.core;

import android.os.Bundle;

/**
 * 消息类实现，可跨进程传递的消息
 * Created by GaoZhen on 2016/5/22.
 */
public class Message extends IMessage.Stub {
    public String mid;
    private Bundle payload;

    @Override
    public void setMid(String mid) {
        this.mid = mid;
    }

    @Override
    public String getMid(){
        return mid;
    }

    @Override
    public void setPayload(Bundle bundle) {
        this.payload = bundle;
    }

    @Override
    public Bundle getPayload(){
        return payload;
    }
}
