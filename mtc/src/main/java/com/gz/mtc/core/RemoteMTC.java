package com.gz.mtc.core;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

/**
 * 远程进程MTC实现
 * Created by GaoZhen on 2016/5/27.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public class RemoteMTC extends BaseMTC {

    private static RemoteMTC instacne;
    private IBridge mainProcessBridge;
    private IBridge selfProcessBridge = new SelfProcessBridge();


    private RemoteMTC() {
    }

    public static RemoteMTC getInstacne() {
        if (instacne == null) {
            synchronized (RemoteMTC.class) {
                if (instacne == null) {
                    instacne = new RemoteMTC();
                }
            }
        }
        return instacne;
    }


    public IBridge getSelfProcessBridge() {
        return selfProcessBridge;
    }

    //***************************以下为IMTC接口实现××××××××××××××××


    @Override
    public boolean registerUnique(String mid, UniqueMsgReceiver receiver) {
        try {
            if (mainProcessBridge == null) {
                return false;
            }
            if (mainProcessBridge.checkGlobleUniqueMsgExist(mid)) {
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
        try {
            if (mainProcessBridge != null) {
                mainProcessBridge.sendMutiIPCMessage(message, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMutiIPCMessage(IMessage message, String processName) {
        try {
            if (mainProcessBridge != null) {
                mainProcessBridge.sendMutiIPCMessage(message, processName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bundle sendUniqueIPCMessage(IMessage message) {
        try {
            if (mainProcessBridge != null) {
                return mainProcessBridge.sendUniqueIPCMessage(message, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Bundle sendUniqueIPCMessage(IMessage message, String processName) {
        try {
            if (mainProcessBridge != null) {
                return mainProcessBridge.sendUniqueIPCMessage(message, processName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendUniqueIPCMessage(IMessage message, IMsgCallback callback) {
        try {
            if (mainProcessBridge != null) {
                mainProcessBridge.sendUniqueIPCMessageAsyc(message, null, callback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUniqueIPCMessage(IMessage message, String processName, IMsgCallback msgCallback) {
        try {
            if (mainProcessBridge != null) {
                mainProcessBridge.sendUniqueIPCMessageAsyc(message, processName, msgCallback);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //××××××××××××××××××××××××××以上IMTC接口实现×××××××××××××××××××××××××××××××


    private class SelfProcessBridge extends IBridge.Stub {
        @Override
        public void sendMutiIPCMessage(IMessage message, String processName) throws RemoteException {
            //远程进程仅实现向自身传进来的跨进程消息，即进程名必须和自身进程名一致
            if (message == null)
                return;
            if (TextUtils.isEmpty(processName) || !processName.equals(ProcessUtil.getProcessName())) {
                return;
            } else {
                RemoteMTC.this.sendMutiMessage(message);
            }
        }

        @Override
        public Bundle sendUniqueIPCMessage(IMessage message, String processName) throws RemoteException {
            if (message == null)
                return null;
            if (TextUtils.isEmpty(processName) || !processName.equals(ProcessUtil.getProcessName())) {
                return null;
            } else {
                return RemoteMTC.this.sendUniqueMessage(message);
            }
        }

        @Override
        public void sendUniqueIPCMessageAsyc(IMessage message, String processName, IMsgCallback callback) throws RemoteException {
            if (message == null)
                return;
            if (TextUtils.isEmpty(processName) || !processName.equals(ProcessUtil.getProcessName())) {
                return;
            } else {
                RemoteMTC.this.sendUniqueMessage(message, callback);
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
            throw new RuntimeException("cannot call this method in remoteProcess");
        }

        @Override
        public void setBridge(IBridge bridge) throws RemoteException {
            mainProcessBridge = bridge;
        }
    }
}
