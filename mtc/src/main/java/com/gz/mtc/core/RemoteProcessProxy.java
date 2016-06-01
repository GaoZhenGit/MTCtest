package com.gz.mtc.core;

import android.content.ServiceConnection;

/**
 * Created by GaoZhen on 2016/5/26.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public class RemoteProcessProxy {
    public String processName = null;
    public Class<? extends RemoteService> clazz = null;
    public IBridge bridge = null;
    public ServiceConnection conn = null;
    public boolean isConnect = false;
}
