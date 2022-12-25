package com.example.tablegame;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Scanner;

public class TestingMultipleThread extends AppCompatActivity {

    int i = 0, count = 0;
    Button buttonResetTextView;
    Button buttonThreadStart;
    TextView textView;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_multiple_thread);

        buttonResetTextView = findViewById(R.id.buttonResetTextView);
        buttonThreadStart = findViewById(R.id.buttonThreadStart);
        textView  = findViewById(R.id.textView);
        textView2  = findViewById(R.id.textView2);

        Thread workerThread;

        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("workerThread", "Printing from worker thread: " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
                for (i=0; i<10; i++){
                    try {
                        Thread.sleep(1000);

                        runOnUiThread( () -> textView2.setText("worker Thread is counting: " + i));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (i==5)
                        Log.i("workerThread", "Worker thread is counting, count reached 5"+ Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());

                }

                Log.i("workerThread", "\"Worker thread has finished "+ Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
            }
        });

        buttonThreadStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workerThread.start();

            }
        });

        buttonResetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count ++;
                textView.setText("Count " + count);
            }
        });
    }
}