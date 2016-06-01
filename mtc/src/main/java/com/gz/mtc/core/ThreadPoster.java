package com.gz.mtc.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池管理类
 * Created by GaoZhen on 2016/5/26.
 * Mail: gaozhen.gz@alibaba-inc.com
 */
public class ThreadPoster {

    static ExecutorService executorService = Executors.newCachedThreadPool();

    private ThreadPoster(){}
    public static void runOnBack(Runnable runnable){
        executorService.execute(runnable);
    }
    public static void runOnMainThread(Runnable runnable){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }
}
