package com.gz.imtc;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gz.mtc.core.IMessage;
import com.gz.mtc.core.IMsgCallback;
import com.gz.mtc.core.MTCManager;
import com.gz.mtc.core.Message;
import com.gz.mtc.core.MsgCallback;
import com.gz.mtc.core.MutiMsgReceiver;
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


        button1.setText("注册独立消息uni1");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UniqueMsgReceiver uniqueMsgReceiver = new UniqueMsgReceiver() {
                    @Override
                    public void onAsynReceive(final IMessage message, IMsgCallback msgCallback) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tag(Tag, "收到独立异步消息：" + message.getMid() + "\n内容："
                                            + (message.getPayload() != null ?
                                            message.getPayload().getString("key") : "null"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString("key", "独立消息回复");
                        if (msgCallback != null) {
                            try {
                                msgCallback.onComplete(bundle);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public Bundle onSynReceive(final IMessage message) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    tag(Tag, "收到独立同步消息：" + message.getMid() + "\n内容："
                                            + (message.getPayload() != null ?
                                            message.getPayload().getString("key") : "null"));
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        Bundle bundle = new Bundle();
                        bundle.putString("key", "独立消息回复");
                        return bundle;
                    }
                };
                MTCManager.getMTC().registerUnique("uni1",uniqueMsgReceiver);
            }
        });
        button2.setText("发送本地独立消息uni1（同步）");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key","data");
                message.setPayload(bundle);
                MTCManager.getMTC().sendUniqueMessage(message);
            }
        });
        button3.setText("发送本地独立消息uni1（异步）");
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key","data");
                message.setPayload(bundle);
                MTCManager.getMTC().sendUniqueMessage(message, new MsgCallback() {
                    @Override
                    public void onComplete(final Bundle bundle) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tag(Tag, "异步消息回调：" + (bundle == null ? null : bundle.getString("key")));
                            }
                        });

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

    public void tag(String tag, String msg) {
        Log.i(tag, msg);
        Toast.makeText(this, tag + ":" + msg, Toast.LENGTH_SHORT).show();
    }
}
