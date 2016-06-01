package com.gz.imtc;

import android.app.Application;

import com.gz.mtc.core.MTCManager;
import com.gz.mtc.core.ProcessUtil;
import com.gz.mtc.core.RemoteService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/5/23.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Map<String,Class<? extends RemoteService>> map = new HashMap<>();
        map.put("com.gz.imtc:remote", MyService.class);
        MTCManager.init(this,map);
    }
}
