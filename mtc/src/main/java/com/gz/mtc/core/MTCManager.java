package com.gz.mtc.core;

import android.app.Application;

import java.util.Map;

/**
 * 表面类，所有api操作入口
 * 根据当前进程实例化IMTC接口
 * Created by GaoZhen on 2016/5/26.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public class MTCManager {
    private static IMTC imtc;
    private static boolean hasInit = false;

    public static IMTC getMTC() {
        if (imtc == null)
            throw new RuntimeException("MTCManager has not inited");
        return imtc;
    }

    public static void init(Application app, Map<String, Class<? extends RemoteService>> remoteServices) {
        if (hasInit)
            return;
        ProcessUtil.setApp(app);
        if (ProcessUtil.isMainProcess()) {
            imtc = new LocalMTC(remoteServices);
        } else {
            imtc = RemoteMTC.getInstacne();
        }
    }
}
