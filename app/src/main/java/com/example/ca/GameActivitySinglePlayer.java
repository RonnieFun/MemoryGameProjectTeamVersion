package com.example.ca;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivitySinglePlayer extends AppCompatActivity {
    int a = 0;
    int playerScore = 0;
    int numberOfColumns = 3;
    int numberOfGameImages;
    int numberOfRows;
    private SoundPool soundPool;
    private int sound1, sound2, sound3;
    ImageView firstChoice;
    private MediaPlayer mediaPlayer;
    private Button pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboardsingle);

        //Instantiate mediaplayer for Canon in D background music
        //Canon in D song mp3 file downloaded from
        //http://down1.5156edu.com/showzipdown.php?id=70229
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(GameActivitySinglePlayer.this, R.raw.softmusic);

        mediaPlayer.setVolume(0.5f, 0.5f);

        //Loop the music
        mediaPlayer.setLooping(true);

        //Start playing the music automatically upon launch of activity
        mediaPlayer.start();

        pauseButton = findViewById(R.id.pauseButtonSingle);
        pauseButton.setOnClickListener(new View.OnClickListener() {
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
        sound1 = soundPool.load(this, R.raw.correct, 1);
        sound2 = soundPool.load(this, R.raw.incorrect, 1);
        sound3 = soundPool.load(this, R.raw.completion, 1);

        //get selected images from LoadImagesActivity explicit intent
        Intent intent = getIntent();
        List<String> ChosenImagesUrls = intent.getStringArrayListExtra("SelectedImagesUrls");
        numberOfGameImages = intent.getIntExtra("numberOfGameImages", 0);
        numberOfRows = (int) Math.ceil((double) numberOfGameImages * 2 / (double) numberOfColumns);
        LinearLayout winGame = findViewById(R.id.WinGame);
        winGame.setVisibility(View.INVISIBLE);


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
        score.setText(getString(R.string.currentScore, 0, numberOfGameImages));

        //load images into game
        {
            LinearLayout gameImages = findViewById(R.id.GameImages);

            for (int a = 0; a < numberOfRows; a++) {
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams lpForRows = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(lpForRows);

                for (int b = 0; b < numberOfColumns; b++) {
                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams lpForImages = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    lpForImages.weight = 1;
                    lpForImages.height = (int) ((this.getResources().getDisplayMetrics().heightPixels) * 0.16);
                    imageView.setLayoutParams(lpForImages);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    linearLayout.addView(imageView);
                }
                runOnUiThread(() -> gameImages.addView(linearLayout));
            }

            int i = 0;
            int y = 0;
            int x = 0;
            while (i < GameImageUrls.size()) {
                LinearLayout ll = (LinearLayout) gameImages.getChildAt(y);
                ImageView emptyImage = (ImageView) ll.getChildAt(x);
                loadImagesForGame(GameImageUrls.get(i), emptyImage);
                i++;
                x++;
                if (x >= numberOfColumns) {
                    y++;
                    x = 0;
                }
            }
        }
    }

    public void loadImagesForGame(String imageUrl, ImageView iv) {
        Glide.with(this).load(imageUrl).into(iv);
        iv.setContentDescription(imageUrl);
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.x));
        iv.setOnClickListener(view -> chooseImage(iv));
    }

    public void chooseImage(ImageView iv) {
        Chronometer GameTimer = findViewById(R.id.TimeElapsed);
        LinearLayout images = findViewById(R.id.GameImages);
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
                    if (b == numberOfGameImages) {
                        score.setText(R.string.completedSmiley);
                        GameTimer.stop();
                        images.postDelayed(() -> showCongratulations(), 500);
                        images.postDelayed(() -> startNewGame(), 6000);
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
        soundPool.play(sound1, 1, 1, 0, 0, 1);
    }

    public void showCross(ImageView iv) {
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.cross));
        soundPool.play(sound2, 1, 1, 0, 0, 1);
    }

    public int scoreUpdate() {
        playerScore++;
        TextView score = findViewById(R.id.Score);
        score.setText(getString(R.string.currentScore, playerScore, numberOfGameImages));
        return playerScore;
    }

    //disable while showing wrong pictures
    public void disableImageSelection() {
        LinearLayout allGameImages = findViewById(R.id.GameImages);
        for (int i = 0; i < allGameImages.getChildCount(); i++) {
            LinearLayout imageRows = (LinearLayout) allGameImages.getChildAt(i);
            for (int j = 0; j < imageRows.getChildCount(); j++) {
                ImageView child = (ImageView) imageRows.getChildAt(j);
                child.setEnabled(false);
            }
        }
    }

    //enable after showing wrong pictures
    public void enableImageSelection(ImageView a, ImageView b) {
        LinearLayout allGameImages = findViewById(R.id.GameImages);
        for (int i = 0; i < allGameImages.getChildCount(); i++) {
            LinearLayout imageRows = (LinearLayout) allGameImages.getChildAt(i);
            for (int j = 0; j < imageRows.getChildCount(); j++) {
                ImageView child = (ImageView) imageRows.getChildAt(j);
                child.setEnabled(true);
            }
            showX(a);
            showX(b);
        }
    }

    public void showCongratulations() {
        mediaPlayer.stop();
        ConstraintLayout scoreConstraint = findViewById(R.id.gameinfoSingle);
        scoreConstraint.setVisibility(View.INVISIBLE);
        ConstraintLayout btnConstraint = findViewById(R.id.btn_constraint);
        btnConstraint.setVisibility(View.INVISIBLE);
        LinearLayout images = findViewById(R.id.GameImages);
        images.setVisibility(View.INVISIBLE);
        LinearLayout winGame = findViewById(R.id.WinGame);
        winGame.setVisibility(View.VISIBLE);
        winGame.bringToFront();
        Chronometer countDownToMainMenu = findViewById(R.id.TimeToNewGame);
        soundPool.play(sound3, 1, 1, 0, 0, 1);
        countDownToMainMenu.setBase(SystemClock.elapsedRealtime() + 5500);
        countDownToMainMenu.start();
    }

    public void startNewGame() {
        Intent intent = new Intent(this, LoadImagesActivity.class);
        startActivity(intent);
    }

    //Pause Music
    public void pauseMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            pauseButton.setText(R.string.resume_music);
        }
    }

    //Resume Music
    public void resumeMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            pauseButton.setText(R.string.pause_music);
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