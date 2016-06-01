package com.gz.mtc.core;

import android.os.Bundle;

/**
 * Created by Administrator on 2016/5/24.
 */
public abstract class MsgCallback extends IMsgCallback.Stub {
    @Override
    public abstract void onComplete(Bundle bundle);

}
