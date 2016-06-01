package com.gz.mtc.core;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Looper;

/**
 * 进程工具类，提供查询方法
 * Created by Gaozhen on 2016/5/21.
 */
public class ProcessUtil {
    private static Boolean isMainProcess = null;
    private static Application app;
    private static String processName;

    public static boolean isMainProcess() {
        if (isMainProcess == null) {
            synchronized (ProcessUtil.class) {
                if (isMainProcess == null) {
                    setProcess();
                }
            }
        }
        return isMainProcess;
    }

    public static String getProcessName() {
        if(processName == null){
            synchronized (ProcessUtil.class){
                int pid = android.os.Process.myPid();
                ActivityManager mActivityManager = (ActivityManager) app
                        .getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                        .getRunningAppProcesses()) {
                    if (appProcess.pid == pid) {
                        processName = appProcess.processName;
                    }
                }
            }
        }

        return processName;
    }

    private static void setProcess() {
        if(app == null){
            throw new RuntimeException("ProcessUtil has not set Application");
        }
        int pid = android.os.Process.myPid();
        String mainProcessName = app.getPackageName();
        ActivityManager mActivityManager = (ActivityManager) app
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                if (appProcess.processName.equals(mainProcessName)) {
                    isMainProcess = true;
                } else {
                    isMainProcess = false;
                }
            }
        }
    }

    public static void setApp(Application app) {
        ProcessUtil.app = app;
    }

    public static Application getApp(){
        if(app == null)
            throw new RuntimeException("null application");
        return app;
    }
    public static boolean isMainThread(){
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
