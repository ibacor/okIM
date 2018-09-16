package com.example.bacor.okim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class MsgMainActivity extends AppCompatActivity{
    private List<MsgBean> msgList = new ArrayList<>();
    private MsgAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.msg_rv_message);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MsgAdapter(msgList,this);
        recyclerView.setAdapter(adapter);

        initReceive();
        initSend();
    }

    private void initSend() {
        final MsgSend msgSend = new MsgSend();
        Button btnSend = findViewById(R.id.msg_btn_send);
        final EditText etInput = findViewById(R.id.msg_et_input);
        final Friend friend = new Friend();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strSendMsg = etInput.getText().toString();
                MsgWrapper msgWrapper = new MsgWrapper(Constants.MSG,friend.getUserId(),friend.getFriend(),strSendMsg);
                msgSend.send(msgWrapper);
            }
        });
    }

    void initReceive(){
        msgList.add(new MsgBean(new MsgWrapper(Constants.MSG,"","","hello"),MsgBean.TYPE_RECEIVED));
        msgList.add(new MsgBean(new MsgWrapper(Constants.MSG,"","","how are you?"),MsgBean.TYPE_RECEIVED));
        msgList.add(new MsgBean(new MsgWrapper(Constants.MSG,"","","I'm fine. Thank you, and you?"),MsgBean.TYPE_SEND));
        msgList.add(new MsgBean(new MsgWrapper(Constants.MSG,"","","I'm fine too!"),MsgBean.TYPE_RECEIVED));

        MsgReceive msgReceive = new MsgReceive();
        msgReceive.setOnReceiveListener(new IReceiveListener() {
            @Override
            public void onReceive(MsgWrapper msgWrapper) {
                MsgBean msgBean = new MsgBean(msgWrapper,MsgBean.TYPE_RECEIVED);
                msgList.add(msgBean);
            }
        });
        msgReceive.startReceive();
    }
}
