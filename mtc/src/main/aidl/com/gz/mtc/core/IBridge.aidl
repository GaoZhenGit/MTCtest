package com.gz.mtc.core;

import com.gz.mtc.core.IMessage;
import com.gz.mtc.core.IBridge;
import com.gz.mtc.core.Bundle;
import com.gz.mtc.core.IMsgCallback;
/**
 * 进程桥接口，进程间通信接口
 * Created by GaoZhen on 2016/5/26.
 * Mail: gaozhen.gz@alibaba-inc.com
 */

interface IBridge {
    void sendMutiIPCMessage(IMessage message, String processName);//进程名为空表示全进程发送，仅远进程调用主进程
    Bundle sendUniqueIPCMessage(IMessage message, String processName);
    void sendUniqueIPCMessageAsyc(IMessage message, String processName,IMsgCallback callback);
    String getProcessName();
    boolean checkUniqueMsgExist(String mid);//实现该进程内是否有该独立消息接收者存在
    boolean checkGlobleUniqueMsgExist(String mid);//仅仅主进程实现，检查全局独立消息接收者存在；
    void setBridge(IBridge bridge);
}
