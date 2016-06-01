package com.gz.imtc;

import android.app.Activity;
import android.content.Intent;
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
import com.gz.mtc.core.UniqueMsgReceiver;

public class MainActivity extends Activity {

    private static final String Tag = "MainActivity";

    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private MutiMsgReceiver mutiMsgReceiver;
    private UniqueMsgReceiver uniqueMsgReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);

        button1.setText("启动远程Activity");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RemoteActivity.class));
            }
        });
        button2.setText("register unique");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uniqueMsgReceiver = new UniqueMsgReceiver() {
                    @Override
                    public void onAsynReceive(IMessage message, IMsgCallback msgCallback) {
                        try {
//                            Log.i("localAct", "bundle:" + message.getPayload().getString("key"));
                            Thread.sleep(3000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "loc->loc,asyn");
                        if (msgCallback != null) {
                            try {
                                msgCallback.onComplete(bundle);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public Bundle onSynReceive(IMessage message) {
                        Bundle bundle = new Bundle();
                        bundle.putString("key", "loc->loc,syn");
                        return bundle;
                    }
                };
                MTCManager.getMTC().registerUnique("rec1", uniqueMsgReceiver);
            }
        });
        button3.setText("send local unique");
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("loc1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "data");
                message.setPayload(bundle);
                Bundle result = MTCManager.getMTC().sendUniqueMessage(message);
                Log.i("localAct", "getSynResult:" + ((result == null || result.get("key") == null) ? null : result.get("key")));
                MTCManager.getMTC().sendUniqueMessage(message, new MsgCallback() {
                    @Override
                    public void onComplete(Bundle result) {
                        Log.i("localAct", "callback:" + ((result == null || result.get("key") == null) ? null : result.get("key")));
                    }
                });
            }
        });

        button4.setText("send ipc syn");
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("rec1");
                Bundle result = MTCManager.getMTC().sendUniqueIPCMessage(message);
                Log.i("localAct", "getIPCSynResult:" + ((result == null || result.get("key") == null) ? null : result.get("key")));
            }
        });

        button5.setText("send ipc Asyn");
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("rec1");
                MTCManager.getMTC().sendUniqueIPCMessage(message,new MsgCallback() {
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
                mutiMsgReceiver = new MutiMsgReceiver() {
                    @Override
                    public void onReceive(IMessage message) {
                        try {
                            Log.i("mainAct","receive:" + message.getMid());
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

        button8.setText("unRegister");
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MTCManager.getMTC().unRegister(mutiMsgReceiver);
                MTCManager.getMTC().unRegister(uniqueMsgReceiver);
            }
        });
    }
}
