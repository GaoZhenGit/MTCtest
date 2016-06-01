package com.gz.mtc.core;

import android.os.Bundle;

/**
 * Created by Administrator on 2016/5/26.
 */
public interface UniqueMsgReceiver {
    void onAsynReceive(IMessage message, IMsgCallback msgCallback);
    Bundle onSynReceive(IMessage message);
}
