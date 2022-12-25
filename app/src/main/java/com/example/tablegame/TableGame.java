package com.example.tablegame;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class TableGame extends AppCompatActivity {

    //CONSTANT INTEGERS FOR COLOURS USED (HEX)
    static final int PURPLE = 0xFF9E02B8;
    static final int GREEN = 0xFF76FF03;
    static final int ORANGE = 0xFFFF5722;
    static final int BUFF = 0xFFED6868;
    static final int MUSTARD_LIGHT = 0x7CFF9100;

    //CONSTANT FOR DIFF MODES OF GAME
    static final int GAME_MODE_LEARN = 0;
    static final int GAME_MODE_KID = 1;
    static final int GAME_MODE_CHALLENGE = 2;
    static final int GAME_MODE_CHALLENGE_PLUS = 3;
    static final int GAME_MODE_CHALLENGE_PLUS_PLUS = 4;

    Thread myWorkingThread;

    LinearLayout tablePlayAreaLayout;
    LinearLayout choiceLayout;
    TextView [] tvTableRow = new TextView[10];
    TextView [] tvChoiceBox = new TextView[5];
    int tvTableRowCurrentIndex = 0, tvChoiceBoxCurrentIndex = 0;


    MediaPlayer gameStatusSound =new MediaPlayer();

    static TextToSpeech tts; //instance of TTS engine, initialized later in code
    boolean ttsInitializedSuccessfully = false;

    ImageView ivGameStatus;
    boolean gameON = false;
    static boolean gameFinishedSuccessfully = false;
    static int tableNumber; //extracted from intent data later in code
    static int tableMultiplier = 1;

    static boolean isSoundOn = true;
    static int gameMode;


    static boolean correctAnswerClicked =false;
    static boolean wrongAnswerClicked = false;
    static boolean ttsIsUtteringTableRow = false;
    Handler myUiThreadHandler;
    Handler myWorkingThreadHandler;
    Looper myWorkingThreadLooper;


    static List<Integer> choiceList = new ArrayList<>();

    @Override
    protected void onStart() {
        super.onStart();

        Log.i("TableGame", "onStart: " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("TableGame", "onStop() " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
       if(ttsInitializedSuccessfully) {
            tts.shutdown();


            //myWorkingThre
        }
        gameStatusSound.release();
        gameON = false;
        //myWorkingThread.interrupt();*/

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        /*Log.i("TableGame", "onPostResume() " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
        gameON = true;
        tvTableRowCurrentIndex = 0;
        tableMultiplier = 1;*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TableGame", "onResume() " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TableGame", "onPause() " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ttsInitializedSuccessfully) {
            tts.shutdown();
            Log.i("TableGame", "onDestroy() " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());

        }
        gameStatusSound.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_game);

        Looper myLooper = Looper.getMainLooper();

        myUiThreadHandler = new Handler(myLooper);


        Log.i("TableGame", "onCreate() " + Thread.currentThread().getName() + " ID: " + Thread.currentThread().getId());

        //Looper.prepare();

        //Extracting parameters of Game from intent data sent by parent layout
        Intent data = getIntent();
        tableNumber= Integer.parseInt(data.getStringExtra("tableNumber"));
        isSoundOn = data.getBooleanExtra("isSoundOn", true);
        gameMode = data.getIntExtra("gameMode",GAME_MODE_LEARN);

        tablePlayAreaLayout = findViewById(R.id.tablePlayAreaLayout);
        tablePlayAreaLayout.setVerticalScrollBarEnabled(true);
        choiceLayout = findViewById(R.id.choiceLayout);

        ivGameStatus = findViewById(R.id.ivGameStatus);
        ivGameStatus.setVisibility(View.GONE);
        tableMultiplier = 1;

        initializeGamePlayLayout();

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Log.i("myWorkingThread", "Inside myWorkingThread " + " from system " + Thread.currentThread().getName());
                myWorkingThreadHandler = new Handler();
                playTableGame();
                Looper.loop();
            }
        };

        myWorkingThread = new Thread(myRunnable);

        Log.i("uiThread", "Working thread state" + myWorkingThread.getState().toString());

        if (isSoundOn){
            tts = new TextToSpeech(TableGame.this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        // Setting speech language
                        int result = tts.setLanguage(Locale.UK);
                        //setting call backmethods for utterance progress
                        setUttereanceListenerFortts();
                        // If your device doesn't support language you set above
                        if (result == TextToSpeech.LANG_MISSING_DATA
                                || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            // Cook simple toast message with message
                            Toast.makeText(getApplicationContext(), "Language not supported",
                                    Toast.LENGTH_LONG).show();
                            Log.e("TTS", "Language is not supported");
                        }
                        else {
                            Log.i("ttsEngine", "ttsEngine initialized " + tableMultiplier);
                            ttsInitializedSuccessfully = true;
                            myWorkingThread.start();
                        }

                    }
                    else {
                        Log.e("TTS", "Initilization Failed");
                        ttsInitializedSuccessfully = false;
                    }
                }
            });


        }
        else   //if Sound is set OFF then start the game without initializing TTS engine.
        {
           ttsInitializedSuccessfully = false;
            myWorkingThread.start();
        }

        //if Sound is set ON for the game then initialize Text to Speech engine with language set to UK English
        //on successful initialization of the TextToSpeech engine also start the game.
        Log.i("UiThread", "Inside UI Thread " + " from system " + Thread.currentThread().getName());

    }



    protected void playTableGame(){

        if(!gameON) {

            correctAnswerClicked = false;
            wrongAnswerClicked = false;
            gameON = true;
            setGameStatusLayout();
            tvTableRowCurrentIndex = 0;
            tvChoiceBoxCurrentIndex = 0;

            if (gameMode == GAME_MODE_LEARN || gameMode == GAME_MODE_KID || gameMode == GAME_MODE_CHALLENGE) {
                tableMultiplier = 1;
                if(gameMode == GAME_MODE_LEARN){
                    correctAnswerClicked = true; //for LEARN mode sets correctAnswerClick to true
                }
                else {
                    setChoiceList();
                    setChoiceBoxView();
                    runOnUiThread( ()->{
                        choiceLayout.setVisibility(View.VISIBLE);
                    });
                }
            }
        }

        if (gameON) {

            if (wrongAnswerClicked && gameMode!=GAME_MODE_LEARN) {
                if(isSoundOn) {
                    gameStatusSound = MediaPlayer.create(TableGame.this, R.raw.gunshot);
                    gameStatusSound.start();
                }
                runOnUiThread(() -> {
                    ivGameStatus.setImageResource(R.drawable.no);
                    ScaleAnimation scaleAnimation = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f);
                    scaleAnimation.setDuration(500l);
                    ivGameStatus.startAnimation(scaleAnimation);
                });
            }

            else if ((tableMultiplier < 10) && (correctAnswerClicked)) {
                if (gameMode == GAME_MODE_LEARN){
                    setAndUtterTableRowView();
                    //setChoiceBoxView();


                    /*tableMultiplier++;
                    tvTableRowCurrentIndex++;
                    if(gameON)
                        playTableGame();*/
                }
                else if (gameMode == GAME_MODE_KID) {
                    if(isSoundOn) {
                        gameStatusSound = MediaPlayer.create(TableGame.this, R.raw.correct);
                        gameStatusSound.start();
                    }
                    runOnUiThread(() -> {
                        ivGameStatus.setImageResource(R.drawable.welldone);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f);
                        scaleAnimation.setDuration(500l);
                        ivGameStatus.startAnimation(scaleAnimation);
                    });
                    setAndUtterTableRowView();

                }
            }

            else if ((tableMultiplier == 10) && correctAnswerClicked) {
                setAndUtterTableRowView();

                myUiThreadHandler.post( new Runnable(){
                    public void run () {
                        ivGameStatus.setImageResource(R.drawable.welldone2);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f);
                        scaleAnimation.setDuration(500l);
                        ivGameStatus.startAnimation(scaleAnimation);
                    }
                });



                gameON = false;

                if(isSoundOn) {
                    gameStatusSound = MediaPlayer.create(TableGame.this, R.raw.greatwelldone);
                    gameStatusSound.start();
                    /*try {

                        Thread.sleep(2500);
                    } catch (InterruptedException e) {}*/
                    gameStatusSound = MediaPlayer.create(TableGame.this, R.raw.claps);
                    gameStatusSound.start();
                }

            }

            else if(!wrongAnswerClicked && !correctAnswerClicked){
                setAndUtterTableRowView();
            }

        }

    }

    public void setAndUtterTableRowView(){

        runOnUiThread( () -> {
            tvTableRow[tvTableRowCurrentIndex].setText(createTableRowText());
        });
        if (correctAnswerClicked){

            runOnUiThread( () ->{

                tvTableRow[tvTableRowCurrentIndex].setBackgroundColor(GREEN);
                tvTableRow[tvTableRowCurrentIndex].setTextColor(PURPLE);
                tvTableRow[tvTableRowCurrentIndex].setVisibility(View.VISIBLE);

            });
            if (ttsInitializedSuccessfully && gameMode == GAME_MODE_LEARN) {
                String callbackId = "myTtsSpeaker";// + tvTableRowCurrentIndex;

                /*Runnable speakerRunnable = () -> tts.speak(tableNumber + "..  " + tableMultiplier + "'s are" + tableNumber * tableMultiplier, TextToSpeech.QUEUE_FLUSH, null, callbackId);
                Thread speakerThread= new Thread(speakerRunnable);
                speakerThread.start();*/
                tts.speak(tableNumber + "..  " + tableMultiplier + "'s are" + tableNumber * tableMultiplier, TextToSpeech.QUEUE_FLUSH, null, callbackId);
                ttsIsUtteringTableRow = true;
                Log.i("myWorkingThread", "tts is speaking " + ttsIsUtteringTableRow);
                /*do {
                    try {
                        Log.i("myWorkingThread", "Sleeping in while() table multiplier " + tableMultiplier);
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                //}*/
                //while (ttsIsUtteringTableRow);
            }
            return;
        }

        else {
            runOnUiThread(() -> {
                tvTableRow[tvTableRowCurrentIndex].setBackgroundColor(PURPLE);//
                tvTableRow[tvTableRowCurrentIndex].setTextColor(GREEN);
                tvTableRow[tvTableRowCurrentIndex].setVisibility(View.VISIBLE);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
                alphaAnimation.setDuration(600l);
                tvTableRow[tvTableRowCurrentIndex].setAlpha(1.0f);
                tvTableRow[tvTableRowCurrentIndex].startAnimation(alphaAnimation);
            });
            if(ttsInitializedSuccessfully) {
                String callbackId = "myTtsSpeaker" + tvTableRowCurrentIndex;
                /*Runnable speakerRunnable = () -> tts.speak(tableNumber + "..  " + tableMultiplier + "'s are", TextToSpeech.QUEUE_FLUSH, null, callbackId);
                Thread speakerThread= new Thread(speakerRunnable);
                speakerThread.start();*/
                tts.speak(tableNumber + "..  " + tableMultiplier + "'s are", TextToSpeech.QUEUE_FLUSH, null, callbackId);
                ttsIsUtteringTableRow = true;
                Log.i("myWorkingThread", "tts is speaking " + ttsIsUtteringTableRow);
                /*do {
                    try {
                        Log.i("myWorkingThread", "Sleeping in while() table multiplier " + tableMultiplier);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                //}*/
                //while (ttsIsUtteringTableRow);
            }
            return;
        }
    }

    public void  setChoiceBoxView(){
        /*if (gameMode == GAME_MODE_LEARN){

            myUiThreadHandler.post( new Runnable(){
                public void run () {
                    for (tvChoiceBoxCurrentIndex = 0; tvChoiceBoxCurrentIndex < 5; tvChoiceBoxCurrentIndex++) {
                        tvChoiceBox[tvChoiceBoxCurrentIndex].setVisibility(View.GONE);
                    }
                    tvChoiceBoxCurrentIndex = 0;
                }
            });

        }*/
        //else {
            runOnUiThread( () -> {

                    for (tvChoiceBoxCurrentIndex = 0; tvChoiceBoxCurrentIndex < 5; tvChoiceBoxCurrentIndex++) {
                        tvChoiceBox[tvChoiceBoxCurrentIndex].setText(choiceList.get(tvChoiceBoxCurrentIndex).toString());

                    }

            });

        //}
    }

    String createTableRowText(){
        String returnString;

        if (gameON == true && correctAnswerClicked == true){
            returnString = " " + tableNumber + " X " + tableMultiplier + " = " + tableNumber*tableMultiplier;
        }
        else {
            returnString = " " + tableNumber + " X " + tableMultiplier + " = ";
        }
        return returnString;

    }

    public static void setChoiceList() {


        choiceList.clear();
        choiceList.add((tableNumber)*(tableMultiplier+1));
        choiceList.add((tableNumber)*(tableMultiplier-1));
        choiceList.add((tableNumber-1)*(tableMultiplier));
        choiceList.add((tableNumber+1)*(tableMultiplier));
        choiceList.add(tableNumber*tableMultiplier);
        Collections.shuffle(choiceList);
    }

     void initializeGamePlayLayout(){


        //tvTableRow is array[10] of TableRow Views. Here we are initializing it & setting visibility to GONE
        for (tvTableRowCurrentIndex=0; tvTableRowCurrentIndex<10; tvTableRowCurrentIndex++){
            tvTableRow[tvTableRowCurrentIndex] = (TextView) (tablePlayAreaLayout.getChildAt(tvTableRowCurrentIndex));
            tvTableRow[tvTableRowCurrentIndex].setVisibility(View.GONE);

        }
        tvTableRowCurrentIndex = 0;

        //tvChocieBox is array[5] of Choice Box Views. Here we are initializing it.
        //Also OnClickListeners are set for all choice boxes to gather if correct answer is clicked by player.
        for (tvChoiceBoxCurrentIndex=0; tvChoiceBoxCurrentIndex<5; tvChoiceBoxCurrentIndex++){
            tvChoiceBox[tvChoiceBoxCurrentIndex] = (TextView) (choiceLayout.getChildAt(tvChoiceBoxCurrentIndex));
            tvChoiceBox[tvChoiceBoxCurrentIndex].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view1) {
                    TextView tv1 = (TextView) view1;
                    if ((Integer.parseInt(tv1.getText().toString()) == tableNumber*tableMultiplier) || gameMode == GAME_MODE_LEARN) {
                        correctAnswerClicked = true;
                        wrongAnswerClicked = false;
                        setAndUtterTableRowView();
                        tableMultiplier++;
                        tvTableRowCurrentIndex++;
                        correctAnswerClicked = false;
                        setChoiceList();
                        setChoiceBoxView();


                        //
                        //playTableGame();
                        myWorkingThreadHandler.post(()->{
                            playTableGame();
                        });

                    }
                    else {
                        correctAnswerClicked = false;
                        wrongAnswerClicked = true;
                        playTableGame();
                        /*myWorkingThreadHandler.post(()->{

                        });*/

                    }

                }

            });
        }
        choiceLayout.setVisibility(View.GONE);
        tvChoiceBoxCurrentIndex = 0;
        tableMultiplier = 1;
    }

    void setGameStatusLayout(){
        if (gameON){
            if (correctAnswerClicked && !gameFinishedSuccessfully){

            }
            else if (wrongAnswerClicked && !gameFinishedSuccessfully){

            }
            else if (gameFinishedSuccessfully){

            }
            else{
                  runOnUiThread( () ->{

                        ivGameStatus.setImageResource(R.drawable.gameison);
                        ivGameStatus.setVisibility(View.VISIBLE);
                        ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1.0f, 0.1f, 1.0f);
                        scaleAnimation.setDuration(500l);
                        ivGameStatus.startAnimation(scaleAnimation);
                        return;
                });

            }
        }
    }
    public void setUttereanceListenerFortts(){
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                //ttsIsUtteringTableRow = true;
                Log.i("ttsOnUtteranceProgressListener", "inside tts.onStart() " + utteranceId + ":" + ttsIsUtteringTableRow);

            }

            @Override
            public void onDone(String utteranceId) {
                ttsIsUtteringTableRow = false;
                Log.i("ttsOnUtteranceProgressListener", "inside tts.onDone() " + utteranceId+ ":" + ttsIsUtteringTableRow);
                if (gameMode == GAME_MODE_LEARN) {
                    tableMultiplier++;
                    tvTableRowCurrentIndex++;
                    if (gameON)
                        playTableGame();
                }
            }

            @Override
            public void onError(String utteranceId) {
                ttsIsUtteringTableRow = false;
                Log.i("ttsOnUtteranceProgressListener", "inside tts.onError() " + utteranceId+ ":" + ttsIsUtteringTableRow);
            }
        });
    }
}