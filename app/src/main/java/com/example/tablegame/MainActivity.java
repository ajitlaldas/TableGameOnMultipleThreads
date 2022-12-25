package com.example.tablegame;

import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity  {


    static final int GAME_MODE_LEARN = 0;
    static final int GAME_MODE_KID = 1;
    static final int GAME_MODE_CHALLENGE = 2;
    static final int GAME_MODE_CHALLENGE_PLUS = 3;
    static final int GAME_MODE_CHALLENGE_PLUS_PLUS = 4;

    EditText etTableNumber;
    Button buttonPlayTableGame;
    Switch isSoundOn;
    RadioGroup radioGroupGameMode;
    RadioButton radioButtonModeLearn;
    RadioButton radioButtonModeKid;
    RadioButton radioButtonModeChallenge;
    RadioButton radioButtonModeChallengePlus;
    RadioButton radioButtonModeChallengePlusPlus;
    ScrollView mainScrollView;

    final int REQUEST_CODE_START_TABLEGAME_ACTIVITY = 1;
    final int MAX_TABLE_NUMBER = 20000;
    //TextToSpeech tts;
    boolean ttsStatus = false;
    static int gameMode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        isSoundOn = findViewById(R.id.isSoundOn);
        isSoundOn.setChecked(true); //default sound is on

        radioButtonModeLearn = findViewById(R.id.radioButtonModeLearn);
        radioButtonModeKid = findViewById(R.id.radioButtonModeKid);
        radioButtonModeChallenge = findViewById(R.id.radioButtonModeChallenge);
        radioButtonModeChallengePlus = findViewById(R.id.radioButtonModeChallengePlus);
        radioButtonModeChallengePlusPlus = findViewById(R.id.radioButtonModeChallengePlusPlus);
        radioButtonModeLearn.setChecked(true); //default is kid mode

        radioGroupGameMode = findViewById(R.id.radioGroupGameMode);

        buttonPlayTableGame =findViewById(R.id.buttonPlayTableGame);

        mainScrollView = findViewById(R.id.mainScrollView);
        //mainActivityLayout.setVerticalScrollBarEnabled(true);
        //mainActivityLayout.setMovementMethod(new ScrollingMovementMethod());

        Intent intentSpeechToText
                = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentSpeechToText.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentSpeechToText.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intentSpeechToText.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");


        etTableNumber = findViewById(R.id.etTableNumber);
        etTableNumber.setText("3");
        etTableNumber.requestFocus();



        /*tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                // TTS is successfully initialized
                if (status == TextToSpeech.SUCCESS) {
                    //tts.speak("Starting Table Game for " + etTableNumber.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);

                    Toast.makeText(MainActivity.this, "TTS Initilization Success", Toast.LENGTH_LONG)
                            .show();
                    // Setting speech language
                    int result = tts.setLanguage(Locale.US);
                    // If your device doesn't support language you set above
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Cook simple toast message with message
                        Toast.makeText(getApplicationContext(), "Language not supported",
                                Toast.LENGTH_LONG).show();
                        Log.e("TTS", "Language is not supported");
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Language Initialized to " + tts.getLanguage().toString(), Toast.LENGTH_LONG)
                                .show();

                    }
                    // Enable the button - It was disabled in main.xml (Go back and
                    // Check it)

                    // TTS is not initialized properly
                } else {
                    Toast.makeText(MainActivity.this, "TTS Initilization Failed", Toast.LENGTH_LONG)
                            .show();
                    Log.e("TTS", "Initilization Failed");
                }
            }
        });*/


        radioGroupGameMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (radioButtonModeLearn.isChecked())
                    gameMode = GAME_MODE_LEARN;
                else if (radioButtonModeKid.isChecked())
                    gameMode = GAME_MODE_KID;
                else if (radioButtonModeChallenge.isChecked())
                    gameMode = GAME_MODE_CHALLENGE;
                else if (radioButtonModeChallengePlus.isChecked())
                    gameMode = GAME_MODE_CHALLENGE_PLUS;
                else if (radioButtonModeChallengePlusPlus.isChecked())
                    gameMode = GAME_MODE_CHALLENGE_PLUS_PLUS;
            }
        });
        buttonPlayTableGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view1) {
                if ((etTableNumber.getText().toString().isEmpty())) {

                    String toastString = "Pl enter some number between 2 & " + MAX_TABLE_NUMBER;
                    Toast.makeText(MainActivity.this, toastString, Toast.LENGTH_SHORT).show();

                } else if ((Integer.parseInt(etTableNumber.getText().toString()) < 2 || Integer.parseInt(etTableNumber.getText().toString()) > MAX_TABLE_NUMBER)) {
                    String toastString = "Pl enter number between 2 & " + MAX_TABLE_NUMBER;
                    Toast.makeText(MainActivity.this, toastString, Toast.LENGTH_SHORT).show();
                } else {
                    if(isSoundOn.isChecked()) {
                            /*tts.speak("Starting Game For " + etTableNumber.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                            while (tts.isSpeaking()) ;*/
                    }
                    Intent intent = new Intent(MainActivity.this, com.example.tablegame.TableGame.class);
                    intent.putExtra("tableNumber", etTableNumber.getText().toString());
                    intent.putExtra("isSoundOn", isSoundOn.isChecked());
                    intent.putExtra("gameMode", gameMode);

                    startActivity(intent);
                    //startActivityForResult(intentSpeechToText, 1);
                }
            }
        });
    }


    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        if(isSoundOn.isChecked()) {
            tts.shutdown();

        }
    }*/

}