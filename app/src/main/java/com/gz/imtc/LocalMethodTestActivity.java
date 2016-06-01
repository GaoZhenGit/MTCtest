package com.gz.imtc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gz.mtc.core.MTCManager;
import com.gz.mtc.core.Message;
import com.gz.mtc.core.MsgCallback;

public class LocalMethodTestActivity extends Activity {

    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.setMid("rec1");
                Bundle bundle = new Bundle();
                bundle.putString("key", "sec");
                msg.setPayload(bundle);
                MTCManager.getMTC().sendMutiMessage(msg);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.setMid("rec3");
                Bundle bundle = MTCManager.getMTC().sendUniqueMessage(msg);
                Log.i("secact", bundle == null ? "null" : bundle.getString("key"));
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.setMid("rec3");
                MTCManager.getMTC().sendUniqueMessage(msg, new MsgCallback() {
                    @Override
                    public void onComplete(Bundle bundle) {
                        Log.i("secact", bundle.getString("key"));
                    }
                });
            }
        });
    }
}
