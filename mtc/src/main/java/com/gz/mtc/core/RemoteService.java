package com.gz.mtc.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by GaoZhen on 2016/5/26.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public class RemoteService extends Service {

    @Override
    final public IBinder onBind(Intent intent) {
        return RemoteMTC.getInstacne().getSelfProcessBridge().asBinder();
    }
}
