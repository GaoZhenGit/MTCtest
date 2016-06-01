package com.gz.imtc;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gz.mtc.core.IMessage;
import com.gz.mtc.core.IMsgCallback;
import com.gz.mtc.core.MTCManager;
import com.gz.mtc.core.Message;
import com.gz.mtc.core.MsgCallback;
import com.gz.mtc.core.MutiMsgReceiver;
import com.gz.mtc.core.ProcessUtil;
import com.gz.mtc.core.UniqueMsgReceiver;

public class RemoteActivity extends Activity {

    private static final String Tag = "RemoteActivity";

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);


        button1.setText("rem->rem,asyn,syn,unique");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("remoteAct", ProcessUtil.getProcessName());
                Message message = new Message();
                message.setMid("rec1");
                Bundle bundle = MTCManager.getMTC().sendUniqueMessage(message);
                Log.i("remoteAct", "getSynResult:" + ((bundle == null || bundle.get("key") == null) ? null : bundle.get("key")));
                MTCManager.getMTC().sendUniqueMessage(message, new MsgCallback() {
                    @Override
                    public void onComplete(Bundle bundle) {
                        Log.i("remoteAct", "callback:" + ((bundle == null || bundle.get("key") == null) ? null : bundle.get("key")));
                    }
                });
            }
        });
        button2.setText("register unimsg");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UniqueMsgReceiver uniqueMsgReceiver = new UniqueMsgReceiver() {
                    @Override
                    public void onAsynReceive(IMessage message, IMsgCallback msgCallback) {
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "rem->rem,asyn");
                        try {
                            Thread.sleep(3000);
                            msgCallback.onComplete(bundle);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public Bundle onSynReceive(IMessage message) {
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "rem->rem,syn");
                        return bundle;
                    }
                };
                MTCManager.getMTC().registerUnique("rec1", uniqueMsgReceiver);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        button4.setText("send ipc syn");
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("rec1");
                Bundle result = MTCManager.getMTC().sendUniqueIPCMessage(message, "com.gz.imtc:remote");
                Log.i("localAct", "getIPCSynResult:" + ((result == null || result.get("key") == null) ? null : result.get("key")));
            }
        });

        button5.setText("send ipc Asyn");
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("rec1");
                MTCManager.getMTC().sendUniqueIPCMessage(message, new MsgCallback() {
                    @Override
                    public void onComplete(Bundle result) {
                        Log.i("localAct", "callback:" + ((result == null || result.get("key") == null) ? null : result.get("key")));
                    }
                });
            }
        });

        button6.setText("register muti");
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MutiMsgReceiver mutiMsgReceiver = new MutiMsgReceiver() {
                    @Override
                    public void onReceive(IMessage message) {
                        try {
                            Log.i("remoteAct","receive:" + message.getMid());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                };
                MTCManager.getMTC().register("mut3",mutiMsgReceiver);
            }
        });

        button7.setText("send muti");
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("mut3");
                MTCManager.getMTC().sendMutiIPCMessage(message);
            }
        });
    }
}
