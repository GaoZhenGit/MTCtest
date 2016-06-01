package com.gz.mtc.core;

import android.os.Bundle;

/**
 * 操控总接口，主进程和非主进程有不同实现
 * Created by GaoZhen on 2016/5/22.
 */
public interface IMTC {
    void register(String mid, MutiMsgReceiver receiver);

    boolean registerUnique(String mid, UniqueMsgReceiver receiver);

    void unRegister(MutiMsgReceiver receiver);

    void unRegister(UniqueMsgReceiver receiver);

    void sendMutiMessage(IMessage message);//发送本进程广播消息，没有返回结果，异步发送

    Bundle sendUniqueMessage(IMessage message);//发送本进程独立消息，同步等待

    void sendUniqueMessage(IMessage message, IMsgCallback callback);//发送本进程独立消息，异步回调

    void sendMutiIPCMessage(IMessage message);//发送跨进程广播消息，全局发送，无返回结果

    void sendMutiIPCMessage(IMessage message, String processName);//发送跨指定进程广播消息，无返回结果

    Bundle sendUniqueIPCMessage(IMessage message);//发送独立消息，在全进程中寻找接受方，同步等待结果

    Bundle sendUniqueIPCMessage(IMessage message, String processName);//发送独立消息，仅在指定进程中寻找接受方，同步等待结果

    void sendUniqueIPCMessage(IMessage message, IMsgCallback callback);//发送独立消息，在全进程中寻找接受方,异步等待结果

    void sendUniqueIPCMessage(IMessage message, String processName, IMsgCallback msgCallback);//发送独立消息，仅在指定进程中寻找接受方，异步等待结果
}
