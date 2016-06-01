package com.gz.mtc.core;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MTC部分实现，完成本地化的发送和注册
 * Created by GaoZhen on 2016/5/27.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public abstract class BaseMTC implements IMTC {

    protected Map<String, List<WeakReference<MutiMsgReceiver>>> mutiMsgReceivers = new ConcurrentHashMap<>();
    protected Map<String, WeakReference<UniqueMsgReceiver>> uniqueMsgReceivers = new ConcurrentHashMap<>();

    @Override
    public void register(String mid, MutiMsgReceiver receiver) {
        if (!mutiMsgReceivers.containsKey(mid)) {
            mutiMsgReceivers.put(mid, new ArrayList<WeakReference<MutiMsgReceiver>>());
        }

        //注册前先清除已经被回收的弱引用
        List<WeakReference<MutiMsgReceiver>> list = mutiMsgReceivers.get(mid);
        Iterator<WeakReference<MutiMsgReceiver>> iterator = list.iterator();
        while (iterator.hasNext()) {
            WeakReference<MutiMsgReceiver> wf = iterator.next();
            if (wf.get() == null) {
                iterator.remove();
            }
        }

        WeakReference<MutiMsgReceiver> wf = new WeakReference<>(receiver);
        list.add(wf);
    }

    @Override
    public void unRegister(UniqueMsgReceiver receiver) {
        Iterator<WeakReference<UniqueMsgReceiver>> iterator = uniqueMsgReceivers.values().iterator();
        while (iterator.hasNext()) {
            WeakReference<UniqueMsgReceiver> wf = iterator.next();
            if (wf.get() != null && wf.get() == receiver) {
                iterator.remove();
                return;//仅删除唯一的接收者
            }
        }
    }

    @Override
    public void unRegister(MutiMsgReceiver receiver) {
        //只做本地取消注册
        for (List<WeakReference<MutiMsgReceiver>> wfList : mutiMsgReceivers.values()) {
            Iterator<WeakReference<MutiMsgReceiver>> iterator = wfList.iterator();
            while (iterator.hasNext()) {
                WeakReference<MutiMsgReceiver> wf = iterator.next();
                if (wf.get() != null && wf.get() == receiver) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void sendMutiMessage(final IMessage message) {
        try {
            String mid = message.getMid();
            if (TextUtils.isEmpty(mid)) {
                return;
            }
            List<WeakReference<MutiMsgReceiver>> list = mutiMsgReceivers.get(mid);
            if (list != null) {
                Iterator<WeakReference<MutiMsgReceiver>> iterator = list.iterator();
                while (iterator.hasNext()) {
                    WeakReference<MutiMsgReceiver> wf = iterator.next();
                    if (wf.get() != null) {
                        final MutiMsgReceiver receiver = wf.get();
                        ThreadPoster.runOnBack(new Runnable() {
                            @Override
                            public void run() {
                                receiver.onReceive(message);
                            }
                        });
                    } else {
                        iterator.remove();//若被回收则清除
                    }
                }
//                for (WeakReference<MutiMsgReceiver> wf : list) {
//                    if (wf.get() != null) {
//                        final MutiMsgReceiver receiver = wf.get();
//                        ThreadPoster.runOnBack(new Runnable() {
//                            @Override
//                            public void run() {
//                                receiver.onReceive(message);
//                            }
//                        });
//                    }
//                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bundle sendUniqueMessage(IMessage message) {
        try {
            String mid = message.getMid();
            if (TextUtils.isEmpty(mid)) {
                return null;
            }
            if (uniqueMsgReceivers.containsKey(mid)) {
                WeakReference<UniqueMsgReceiver> wf = uniqueMsgReceivers.get(mid);
                if (wf.get() != null) {
                    UniqueMsgReceiver uniqueMsgReceiver = wf.get();
                    return uniqueMsgReceiver.onSynReceive(message);
                } else {
                    uniqueMsgReceivers.remove(mid);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendUniqueMessage(final IMessage message, final IMsgCallback callback) {
        try {
            String mid = message.getMid();

            if (TextUtils.isEmpty(mid)) {
                return;
            }

            if (uniqueMsgReceivers.containsKey(mid)) {
                WeakReference<UniqueMsgReceiver> wf = uniqueMsgReceivers.get(mid);
                if (wf.get() != null) {
                    final UniqueMsgReceiver uniqueMsgReceiver = wf.get();
                    ThreadPoster.runOnBack(new Runnable() {
                        @Override
                        public void run() {
                            uniqueMsgReceiver.onAsynReceive(message, callback);
                        }
                    });
                } else {
                    uniqueMsgReceivers.remove(mid);
                }
                return;//只发送给第一个（也仅有一个）接受者
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


}
