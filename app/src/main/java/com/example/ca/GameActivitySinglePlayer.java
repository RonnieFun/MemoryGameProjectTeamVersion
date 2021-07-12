package com.example.ca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivitySinglePlayer extends AppCompatActivity {
    int a = 0;
    int playerScore = 0;
    private SoundPool soundPool;
    private int sound1, sound2, sound3;
    ImageView firstChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboardsingle);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();

        sound1 = soundPool.load(this,R.raw.correct, 1);
        sound2 = soundPool.load(this,R.raw.incorrect, 1);
        sound3 = soundPool.load(this, R.raw.completion, 1);

        //get selected images from LoadImagesActivity explicit intent
        Intent intent = getIntent();
        List<String> ChosenImagesUrls = intent.getStringArrayListExtra("SelectedImagesUrls");

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
        score.setText(getString(R.string.currentScore, 0));

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
        ConstraintLayout images = findViewById(R.id.images);
        TextView score = findViewById(R.id.Score);
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
                    int b = scoreUpdate();
                    if (b == 6) {
                        score.setText(R.string.completedSmiley);
                        GameTimer.stop();
                        showCongratulations();
                        images.postDelayed(() -> startNewGame(), 5000);
                    }

                } else {
                    showCross(firstChoice);
                    showCross(iv);
                    disableImageSelection();
                    images.postDelayed(() ->
                            enableImageSelection(firstChoice, iv), 1000);
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

    public int scoreUpdate() {
        playerScore++;
        TextView score = findViewById(R.id.Score);
        score.setText(getString(R.string.currentScore, playerScore));
        return playerScore;
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

    public void showCongratulations() {
        LinearLayout winGame = findViewById(R.id.WinGame);
        winGame.bringToFront();
        Chronometer countDownToMainMenu = findViewById(R.id.TimeToNewGame);
        soundPool.play(sound3, 1, 1, 0,0,1);
        countDownToMainMenu.setBase(SystemClock.elapsedRealtime()+5000);
        countDownToMainMenu.start();
    }

    public void startNewGame() {
        Intent intent = new Intent(this, LoadImagesActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}