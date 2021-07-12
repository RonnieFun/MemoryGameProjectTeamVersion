package com.example.ca;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivityDoublePlayer extends AppCompatActivity {
    int a = 0;
    int player1Score = 0;
    int player2Score = 0;
    int turn = 1;
    long timeWhenGameTimer1Stopped = 0;
    long timeWhenGameTimer2Stopped = 0;
    private SoundPool soundPool;
    private int sound1, sound2, sound3;
    ImageView firstChoice;
    private MediaPlayer mediaPlayer;
    private Button pauseButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboarddouble);

        //Instantiate mediaplayer for mario background music
        //mario medley song mp3 file downloaded from
        //https://play.nintendo.com/printables/uncategorized/exclusive-download-super-mario-bros-song/
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(GameActivityDoublePlayer.this, R.raw.super_mario_medley);

        mediaPlayer.setVolume(0.5f, 0.5f);
        //Loop the music
        mediaPlayer.setLooping(true);

        //Start playing the music automatically upon launch of activity
        mediaPlayer.start();

        pauseButton2 = findViewById(R.id.pauseButtonDouble);
        pauseButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    pauseMusic();
                } else {
                    resumeMusic();
                }
            }
        });

        //Instantiate soundpool for soundeffects
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        // Sound effect mp3 files downloaded from www.zapsplat.com
        sound1 = soundPool.load(this,R.raw.correct, 1);
        sound2 = soundPool.load(this,R.raw.incorrect, 1);
        sound3 = soundPool.load(this, R.raw.completion, 1);

        //get selected images from LoadImagesActivity explicit intent
        Intent intent = getIntent();
        List<String> ChosenImagesUrls = intent.getStringArrayListExtra("SelectedImagesUrls2");

        //put urls + duplicate in new list
        List<String> GameImageUrls = new ArrayList<>();
        if (ChosenImagesUrls != null) {
            GameImageUrls.addAll(ChosenImagesUrls);
            GameImageUrls.addAll(ChosenImagesUrls);
        }

        //shuffle to randomise
        Collections.shuffle(GameImageUrls);

        //initialise scoreboard
        TextView score = findViewById(R.id.Score);
        score.setText(getString(R.string.currentScoreP1, 0));

        TextView score2 = findViewById(R.id.Score2);
        score2.setText(getString(R.string.currentScoreP2, 0));

        //load images into game
        for (int i = 21; i <= 32; i++) {
            String a = "imageView" + i;
            int emptyImageId = getResources().getIdentifier(a, "id", getPackageName());
            ImageView emptyImage = findViewById(emptyImageId);
            loadImagesForGame(GameImageUrls.get(i - 21), emptyImage);
        }
    }

    public void loadImagesForGame(String imageUrl, ImageView iv) {
        Picasso.get().load(imageUrl).fit().into(iv);
        iv.setContentDescription(imageUrl);
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.x));
        iv.setOnClickListener(view -> chooseImage(iv));
    }

    public void chooseImage(ImageView iv) {
        Chronometer GameTimer = findViewById(R.id.TimeElapsed);
        Chronometer GameTimer2 = findViewById(R.id.TimeElapsed2);
        ConstraintLayout images = findViewById(R.id.images);
        TextView score = findViewById(R.id.Score);
        TextView score2 = findViewById(R.id.Score2);

        {
            if (firstChoice == null) {
                GameTimer.start();
            }
            a++;
            showImage(iv);
            if (a == 2) {
                if (firstChoice.getContentDescription() == iv.getContentDescription()) {
                    showTick(firstChoice);
                    firstChoice.setOnClickListener(null);
                    showTick(iv);
                    iv.setOnClickListener(null);
                    int b = 0;
                    int c = 0;
                    if (turn == 1) {
                        score.setTextColor(Color.GRAY);
                        score2.setTextColor(Color.BLACK);
                        b = scoreUpdatePlayer1();
                        turn = 2;
                        timeWhenGameTimer1Stopped = GameTimer.getBase() - SystemClock.elapsedRealtime();
                        GameTimer.stop();
                        GameTimer2.setBase(SystemClock.elapsedRealtime() + timeWhenGameTimer2Stopped);
                        GameTimer2.start();
                        if (b == 4) {
                            score.setText(R.string.completedSmileyPlayer1);
                            GameTimer.stop();
                            ConstraintLayout imageConstraint = findViewById(R.id.gameinfoDouble);
                            imageConstraint.setVisibility(View.INVISIBLE);
                            ConstraintLayout btnConstraint = findViewById(R.id.btn_constraint);
                            btnConstraint.setVisibility(View.INVISIBLE);
                            showCongratulationsPlayer1();
                            images.postDelayed(() -> startNewGame(), 5000);
                        }
                        if (player1Score == 3 && player2Score == 3) {
                            GameTimer.stop();
                            GameTimer2.stop();
                            showDrawGame();
                            images.postDelayed(() -> startNewGame(),5000);
                        }

                    } else if (turn == 2){
                        score.setTextColor(Color.BLACK);
                        score2.setTextColor(Color.GRAY);
                        c = scoreUpdatePlayer2();
                        turn = 1;
                        timeWhenGameTimer2Stopped = GameTimer2.getBase() - SystemClock.elapsedRealtime();
                        GameTimer2.stop();
                        GameTimer.setBase(SystemClock.elapsedRealtime() + timeWhenGameTimer1Stopped);
                        GameTimer.start();
                        if (c == 4) {
                            score2.setText((R.string.completedSmileyPlayer2));
                            GameTimer2.stop();
                            ConstraintLayout imageConstraint = findViewById(R.id.gameinfoDouble);
                            imageConstraint.setVisibility(View.INVISIBLE);
                            ConstraintLayout btnConstraint = findViewById(R.id.btn_constraint);
                            btnConstraint.setVisibility(View.INVISIBLE);
                            showCongratulationsPlayer2();
                            ;
                            images.postDelayed(() -> startNewGame(), 5000);
                        }
                        if (player1Score == 3 && player2Score == 3) {
                            GameTimer.stop();
                            GameTimer2.stop();
                            showDrawGame();
                            images.postDelayed(() -> startNewGame(),5000);
                        }

                    }

                } else {
                    showCross(firstChoice);
                    showCross(iv);
                    disableImageSelection();
                    images.postDelayed(() ->
                            enableImageSelection(firstChoice, iv), 500);
                    if (turn == 1) {
                        score.setTextColor(Color.GRAY);
                        score2.setTextColor(Color.BLACK);
                        turn = 2;
                        timeWhenGameTimer1Stopped = GameTimer.getBase() - SystemClock.elapsedRealtime();
                        GameTimer.stop();
                        GameTimer2.setBase(SystemClock.elapsedRealtime() + timeWhenGameTimer2Stopped);
                        GameTimer2.start();
                    } else if (turn == 2) {
                        score.setTextColor(Color.BLACK);
                        score2.setTextColor(Color.GRAY);
                        turn = 1;
                        timeWhenGameTimer2Stopped = GameTimer2.getBase() - SystemClock.elapsedRealtime();
                        GameTimer2.stop();
                        GameTimer.setBase(SystemClock.elapsedRealtime() + timeWhenGameTimer1Stopped);
                        GameTimer.start();
                    }
                }
                a = 0;

            } else if (a == 1) {
                firstChoice = iv;
                firstChoice.setEnabled(false);
            }
        }
    }

    public void showImage(ImageView iv) {
        iv.setForeground(null);
    }

    public void showX(ImageView iv) {
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.x));
    }

    public void showTick(ImageView iv) {
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.tick));
        soundPool.play(sound1, 1, 1, 0,0,1);
    }

    public void showCross(ImageView iv) {
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.cross));
        soundPool.play(sound2, 1, 1, 0,0,1);
    }

    public int scoreUpdatePlayer1() {
        player1Score++;
        TextView score = findViewById(R.id.Score);
        score.setText(getString(R.string.currentScoreP1, player1Score));
        return player1Score;
    }

    public int scoreUpdatePlayer2() {
        player2Score++;
        TextView score2 = findViewById(R.id.Score2);
        score2.setText(getString(R.string.currentScoreP2, player2Score));
        return player2Score;
    }

    //disable while showing wrong pictures
    public void disableImageSelection() {
        ConstraintLayout images = findViewById(R.id.images);
        for (int i = 0; i < images.getChildCount(); i++) {
            View child = images.getChildAt(i);
            child.setEnabled(false);
        }
    }

    //enable after showing wrong pictures
    public void enableImageSelection(ImageView a, ImageView b) {
        ConstraintLayout images = findViewById(R.id.images);
        for (int i = 0; i < images.getChildCount(); i++) {
            View child = images.getChildAt(i);
            if (child.getForeground() != AppCompatResources.getDrawable(this, R.drawable.tick))
                child.setEnabled(true);
        }

        showX(a);
        showX(b);
    }

    public void showCongratulationsPlayer1() {
        LinearLayout winGame1 = findViewById(R.id.Player1WinGame);
        winGame1.bringToFront();
        Chronometer countDownToMainMenu = findViewById(R.id.TimeToNewGame1);
        soundPool.play(sound3, 1, 1, 0,0,1);
        countDownToMainMenu.setBase(SystemClock.elapsedRealtime()+5000);
        countDownToMainMenu.start();
    }

    public void showCongratulationsPlayer2() {
        LinearLayout winGame2 = findViewById(R.id.Player2WinGame);
        winGame2.bringToFront();
        Chronometer countDownToMainMenu = findViewById(R.id.TimeToNewGame2);
        soundPool.play(sound3, 1, 1, 0,0,1);
        countDownToMainMenu.setBase(SystemClock.elapsedRealtime()+5000);
        countDownToMainMenu.start();
    }

    public void showDrawGame() {
        LinearLayout drawnGame = findViewById(R.id.DrawnGame);
        drawnGame.bringToFront();
        Chronometer countDownToMainMenu = findViewById(R.id.TimeToNewGame3);
        soundPool.play(sound3, 1, 1, 0,0,1);
        countDownToMainMenu.setBase(SystemClock.elapsedRealtime()+5000);
        countDownToMainMenu.start();
    }

    public void startNewGame() {
        Intent intent = new Intent(this, LoadImagesActivity.class);
        startActivity(intent);
        mediaPlayer.stop();
    }

    //Pause Music
    public void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            pauseButton2.setText(R.string.resume_music);
        }
    }

    //Resume Music
    public void resumeMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            pauseButton2.setText(R.string.pause_music);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.release();
        }
    }
}