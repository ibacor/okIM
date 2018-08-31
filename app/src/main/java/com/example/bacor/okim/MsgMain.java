package com.example.bacor.okim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MsgMain extends AppCompatActivity{
    private List<MsgBean> msgList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_main);

        init();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.msg_rv_message);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        MsgAdapter adapter = new MsgAdapter(msgList,this);
        recyclerView.setAdapter(adapter);
    }

    void init(){
        msgList.add(new MsgBean("hello",MsgBean.TYPE_RECEIVED));
        msgList.add(new MsgBean("how are you?",MsgBean.TYPE_RECEIVED));
        msgList.add(new MsgBean("I'm fine. Thank you, and you?",MsgBean.TYPE_SEND));
        msgList.add(new MsgBean("I'm fine too!",MsgBean.TYPE_RECEIVED));
    }
}
