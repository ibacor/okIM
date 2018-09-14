package com.example.bacor.okim;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ExecutorService threadPool;
    Socket socket;
    InputStream is;
    InputStreamReader isr;
    BufferedReader br;
    OutputStream os;
    OutputStreamWriter osw;
    BufferedWriter bw;
    String host = "30.7.81.16";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        threadPool = Executors.newCachedThreadPool();

        Button button = (Button) findViewById(R.id.main_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,MsgMainActivity.class);
                startActivity(intent);
            }
        });

        Button connect = findViewById(R.id.main_btn_connect);
        Button send = findViewById(R.id.main_btn_send);
        Button disconnect = findViewById(R.id.main_btn_disconnect);
        final EditText etSend = findViewById(R.id.main_et_send);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(host,8878);
                            if(socket.isConnected())
                                Log.d("bacor","server connect success");
                            else
                                Log.d("bacor","server connect failed");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket.close();
                            if(!socket.isConnected())
                                Log.d("bacor","server disconnect success");
                            else
                                Log.d("bacor","server disconnect failed");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket(host,8878);
                            if(socket.isConnected())
                                Log.d("bacor","server connect success");
                            else
                                Log.d("bacor","server connect failed");

                            os = socket.getOutputStream();
                            osw = new OutputStreamWriter(os);
                            bw = new BufferedWriter(osw);
                            String writeString = etSend.getText().toString();
                            bw.write(writeString + "\n");
                            bw.flush();

                            is = socket.getInputStream();
                            isr = new InputStreamReader(is);
                            br = new BufferedReader(isr);
                            String response = br.readLine();
                            Log.d("bacor","response: "+response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
