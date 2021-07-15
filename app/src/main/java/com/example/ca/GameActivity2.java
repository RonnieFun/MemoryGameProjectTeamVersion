package com.example.ca;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity2 extends AppCompatActivity {
    int a = 0;
    int playerScore = 0;
    int numberOfColumns = 3;
    int numberOfGameImages;
    ImageView firstChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboard2);

        //get selected images from LoadImagesActivity explicit intent
        Intent intent = getIntent();
        List<String> ChosenImagesUrls = intent.getStringArrayListExtra("SelectedImagesUrls");
        numberOfGameImages = intent.getIntExtra("numberOfGameImages", 0);

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

            for (int a = 0; a < Math.ceil((double)GameImageUrls.size() / (double)numberOfColumns); a++) {
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams lpForRows = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setLayoutParams(lpForRows);

                for (int b = 0; b < numberOfColumns; b++) {
                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams lpForImages = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                    lpForImages.weight = 1;
                    lpForImages.height = (int) ((this.getResources().getDisplayMetrics().heightPixels) * 0.18);
                    imageView.setLayoutParams(lpForImages);
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
        Picasso.get().load(imageUrl).fit().into(iv);
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
    }

    public void showCross(ImageView iv) {
        iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.cross));
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
        LinearLayout winGame = findViewById(R.id.WinGame);
        winGame.bringToFront();
        Chronometer countDownToMainMenu = findViewById(R.id.TimeToNewGame);
        countDownToMainMenu.setBase(SystemClock.elapsedRealtime() + 5000);
        countDownToMainMenu.start();
    }

    public void startNewGame() {
        Intent intent = new Intent(this, LoadImagesActivity2.class);
        startActivity(intent);
    }
}