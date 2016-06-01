package com.gz.mtc.core;

interface IMessage {
    void setMid(String mid);
    String getMid();
    void setPayload(inout Bundle bundle);
    Bundle getPayload();
}
