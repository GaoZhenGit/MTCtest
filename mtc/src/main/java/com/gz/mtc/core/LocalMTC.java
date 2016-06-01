package com.gz.mtc.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主进程实现的MTC类，承担注册发送消息的功能，同时担任多进程调配中心
 * Created by GaoZhen on 2016/5/26.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public class LocalMTC extends BaseMTC {
    private Map<String, RemoteProcessProxy> remoteProxys = new ConcurrentHashMap<>();
    private IBridge mainProcessBridge = new MainProcessBridge();
    Object lock = new Object();

    public LocalMTC(Map<String, Class<? extends RemoteService>> remoteServices) {
        if (remoteServices == null) {
            return;
        }
        for (Map.Entry<String, Class<? extends RemoteService>> entry : remoteServices.entrySet()) {
            final RemoteProcessProxy proxy = new RemoteProcessProxy();
            proxy.processName = entry.getKey();
            proxy.clazz = entry.getValue();
            remoteProxys.put(entry.getKey(), proxy);
            startRemoteServiceIfNotConnect(entry.getKey(), new ConnectedCallback() {
                @Override
                public void connected() {
                    Log.i("proxy", "connect");
//                    try {
//                        remoteProxys.put(proxy.bridge.getProcessName(), proxy);
//                    } catch (RemoteException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }
    }

    @Override
    public boolean registerUnique(String mid, UniqueMsgReceiver receiver) {
        try {
            if (mainProcessBridge.checkGlobleUniqueMsgExist(mid)) {
                //判断其他进程是否含有该mid订阅者
                return false;
            }
            WeakReference<UniqueMsgReceiver> wf = new WeakReference<>(receiver);
            uniqueMsgReceivers.put(mid, wf);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void sendMutiIPCMessage(IMessage message) {
        sendMutiMessage(message);//先在自身进程里发送
        for (Map.Entry<String, RemoteProcessProxy> entry : remoteProxys.entrySet()) {//逐个进程发送
            sendMutiIPCMessage(message, entry.getKey());
        }
    }

    @Override
    public void sendMutiIPCMessage(final IMessage message, final String processName) {
        if (ProcessUtil.getProcessName().equals(processName)) {//若指定主进程
            sendMutiMessage(message);
            return;
        }
        final RemoteProcessProxy proxy = remoteProxys.get(processName);
        if (proxy != null) {
            if (proxy.isConnect) {//判断远程进程service是否存活
                //如果是，直接发送消息
                ThreadPoster.runOnBack(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            proxy.bridge.sendMutiIPCMessage(message, processName);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                //未连接则发起连接
                final ConnectedCallback connectedCallback = new ConnectedCallback() {
                    //连接完成后再发起发送
                    @Override
                    public void connected() {
                        ThreadPoster.runOnBack(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.i("loalMtc", "connected");
                                    proxy.bridge.sendMutiIPCMessage(message, processName);
                                    Log.i("loalMtc", "first send");
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };
                startRemoteServiceIfNotConnect(processName, connectedCallback);
            }
        }
    }

    @Override
    public Bundle sendUniqueIPCMessage(IMessage message) {
        try {
            if (mainProcessBridge.checkUniqueMsgExist(message.getMid())) {
                //先在主进程内寻找接收者
                return sendUniqueMessage(message);
            }
            //在每个进程内寻找接收者
            for (RemoteProcessProxy proxy : remoteProxys.values()) {
                if (proxy.isConnect && proxy.bridge.checkUniqueMsgExist(message.getMid())) {//检查接收者在哪个进程里面
                    return proxy.bridge.sendUniqueIPCMessage(message, proxy.processName);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Bundle sendUniqueIPCMessage(IMessage message, final String processName) {
        if (processName.equals(ProcessUtil.getProcessName())) {
            //判断接收者是否在主进程内
            return sendUniqueMessage(message);
        }
        //再指定进程内判断是否有接收者
        final RemoteProcessProxy proxy = remoteProxys.get(processName);
        if (proxy != null && proxy.isConnect) {
            try {
                return proxy.bridge.sendUniqueIPCMessage(message, processName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void sendUniqueIPCMessage(IMessage message, IMsgCallback callback) {
        try {
            if (mainProcessBridge.checkUniqueMsgExist(message.getMid())) {
                sendUniqueMessage(message, callback);//先发送本地进程
                return;
            } else {
                for (RemoteProcessProxy proxy : remoteProxys.values()) {
                    sendUniqueIPCMessage(message, proxy.processName, callback);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUniqueIPCMessage(final IMessage message, final String processName, final IMsgCallback msgCallback) {
        if (processName.equals(ProcessUtil.getProcessName())) {
            //判断接收者是否在主进程内
            sendUniqueMessage(message, msgCallback);
            return;
        }
        final RemoteProcessProxy proxy = remoteProxys.get(processName);
        if (proxy != null && proxy.isConnect) {
            ThreadPoster.runOnBack(new Runnable() {
                @Override
                public void run() {
                    try {
                        proxy.bridge.sendUniqueIPCMessageAsyc(message, processName, msgCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            startRemoteServiceIfNotConnect(processName, new ConnectedCallback() {
                @Override
                public void connected() {
                    if (proxy != null && proxy.isConnect) {
                        ThreadPoster.runOnBack(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    proxy.bridge.sendUniqueIPCMessageAsyc(message, processName, msgCallback);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void startRemoteServiceIfNotConnect(String processName, final ConnectedCallback callback) {
        final RemoteProcessProxy proxy = remoteProxys.get(processName);
        if (proxy != null) {
            proxy.conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    proxy.bridge = IBridge.Stub.asInterface(service);//获得远程进程代理
                    proxy.isConnect = true;
                    try {
                        proxy.bridge.setBridge(mainProcessBridge);//将主进程代理发送至远程进程使用
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (callback != null) {
                        callback.connected();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    proxy.bridge = null;
                    proxy.isConnect = false;
                }
            };
            Intent intent = new Intent(ProcessUtil.getApp(), proxy.clazz);
            ProcessUtil.getApp().bindService(intent, proxy.conn, Context.BIND_AUTO_CREATE);//发起远程进程连接
        }

    }

    /**
     * 主进程代理类，供远程进程调用
     */
    public class MainProcessBridge extends IBridge.Stub {

        @Override
        public void sendMutiIPCMessage(IMessage message, String processName) throws RemoteException {
            if (message == null)
                return;
            if (TextUtils.isEmpty(processName)) {
                //如果远程进程发送至此，未指定进程名，则在主进程和其他进程也发送广播消息
                LocalMTC.this.sendMutiMessage(message);
                LocalMTC.this.sendMutiIPCMessage(message);
            } else {
                if (processName.equals(ProcessUtil.getProcessName())) {
                    LocalMTC.this.sendMutiMessage(message);
                } else {
                    LocalMTC.this.sendMutiIPCMessage(message, processName);
                }
            }
        }

        @Override
        public Bundle sendUniqueIPCMessage(IMessage message, String processName) throws RemoteException {
            if (message == null) {
                return null;
            }

            if (TextUtils.isEmpty(processName)) {
                return LocalMTC.this.sendUniqueIPCMessage(message);
            } else {
                return LocalMTC.this.sendUniqueIPCMessage(message, processName);
            }
        }

        @Override
        public void sendUniqueIPCMessageAsyc(IMessage message, String processsName, IMsgCallback callback) throws RemoteException {
            if (TextUtils.isEmpty(processsName)) {
                LocalMTC.this.sendUniqueIPCMessage(message, callback);
            } else {
                LocalMTC.this.sendUniqueIPCMessage(message, processsName, callback);
            }

        }

        @Override
        public String getProcessName() throws RemoteException {
            return ProcessUtil.getProcessName();
        }

        @Override
        public boolean checkUniqueMsgExist(String mid) throws RemoteException {
            if (uniqueMsgReceivers.get(mid) != null) {
                WeakReference<UniqueMsgReceiver> wf = uniqueMsgReceivers.get(mid);
                if (wf.get() == null) {//如果弱引用已经被回收，则判断为不存在
                    uniqueMsgReceivers.remove(mid);
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean checkGlobleUniqueMsgExist(String mid) throws RemoteException {
            if (checkUniqueMsgExist(mid)) {
                //检查本地进程是否含有mid接收者
                return true;
            }
            for (RemoteProcessProxy proxy : remoteProxys.values()) {
                //检查每个远程进程进程内是否包含该mid接收者
                if (proxy.bridge.checkUniqueMsgExist(mid)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void setBridge(IBridge bridge) throws RemoteException {

        }
    }

    /**
     * 该接口为内部接口，用于连接远程进程service后调用
     */
    private interface ConnectedCallback {
        void connected();
    }
}
