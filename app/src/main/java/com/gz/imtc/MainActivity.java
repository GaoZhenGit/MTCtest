package com.gz.imtc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private EditText editText1;
    private Button button7;
    private Button button8;
    private Button button9;
    private Button button10;
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
        editText1 = (EditText) findViewById(R.id.edittext1);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);
        button10 = (Button) findViewById(R.id.button10);

        button1.setText("启动远程Activity");
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RemoteActivity.class));
            }
        });
        button2.setText("注册独立消息：uni1");
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uniqueMsgReceiver = new UniqueMsgReceiver() {
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
                boolean isSuccess = MTCManager.getMTC().registerUnique("uni1", uniqueMsgReceiver);
                if (isSuccess) {
                    tag(Tag, "注册成功");
                } else {
                    tag(Tag, "注册失败");
                }
            }
        });
        button3.setText("发送本地独立消息uni1（同步）");
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "data");
                message.setPayload(bundle);
                Bundle result = MTCManager.getMTC().sendUniqueMessage(message);
            }
        });
        button4.setText("发送本地独立消息uni1（异步）");
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "data");
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

        button5.setText("发送匿名ipc独立消息uni1（同步）");
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "IpcData");
                message.setPayload(bundle);
                MTCManager.getMTC().sendUniqueIPCMessage(message);
            }
        });

        button6.setText("发送匿名ipc独立消息uni1（异步）");
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "IpcData");
                message.setPayload(bundle);
                MTCManager.getMTC().sendUniqueIPCMessage(message, new MsgCallback() {
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

        button7.setText("发送指定ipc独立消息uni1（同步）");
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "data");
                message.setPayload(bundle);
                MTCManager.getMTC().sendUniqueIPCMessage(message, editText1.getText().toString());
            }
        });
        editText1.setHint("进程名");

        button8.setText("发送指定ipc独立消息uni1（异步）");
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.setMid("uni1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "data");
                message.setPayload(bundle);
                MTCManager.getMTC().sendUniqueIPCMessage(message, editText1.getText().toString(), new MsgCallback() {
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
    }

    public void tag(String tag, String msg) {
        Log.i(tag, msg);
        Toast.makeText(this, tag + ":" + msg, Toast.LENGTH_SHORT).show();
    }
}
