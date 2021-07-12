package com.example.ca;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class LoadImagesActivity extends AppCompatActivity {

    private Elements imagesDownload;
    private Document website;
    private ProgressBar downloadProgressBar;
    private TextView downloadProgressText;
    private TextView proceedToSingleGame;
    private TextView proceedToDoubleGame;
    private final Collection<ImageView> selectedImages = new ArrayList<>();
    private boolean buttonPressedAgain = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadimages);

        Button getUrl = findViewById(R.id.FetchButton);
        getUrl.setOnClickListener((view) -> GetImagesFromUrl());

        Intent intent = getIntent();
        EditText enteredUrlRaw = findViewById(R.id.EnteredUrl);
        enteredUrlRaw.setText(intent.getStringExtra("EnteredUrl"));

        downloadProgressText = findViewById(R.id.DownloadProgress);
        proceedToSingleGame = findViewById(R.id.ProceedToSinglePlayerGame);
        proceedToDoubleGame = findViewById(R.id.ProceedToDoublePlayerGame);

        getUrl.bringToFront();
        downloadProgressText.setText(R.string.awaitingUrlInput);
        downloadProgressText.bringToFront();

        if (intent.getStringExtra("EnteredUrl") != null) {
            GetImagesFromUrl();
        }
    }

    @SuppressLint("SetTextI18n")
    public void GetImagesFromUrl() {
        //set up alternate Fetch button
        Button getUrlWhileScraping = findViewById(R.id.FetchButtonWhileScraping);
        getUrlWhileScraping.setOnClickListener((view) -> fetchButtonPressedWhileLoadingImages());
        getUrlWhileScraping.bringToFront();

        //load placeholder images
        ConstraintLayout loadImagesViewImages = findViewById(R.id.Images);
        for (int i = 0; i < loadImagesViewImages.getChildCount(); i++) {
            Picasso.get().load(R.drawable.x).fit().into((ImageView) loadImagesViewImages.getChildAt(i));
        }

        //scrape website and get images on new thread so main thread can load progress
        new Thread(() -> {
            {
                //set up progress bar and status
                downloadProgressBar = findViewById(R.id.DownloadProgressBar);
                downloadProgressText.setText("Checking the website...");

                hideKeyboard();
                getWebsite();

                //get images from website
                imagesDownload = website.getElementsByTag("img");
                int i = 1;
                int j = 0;
                while (i <= 20 && !buttonPressedAgain) {
                    if (imagesDownload.get(j).attr("src") != null && imagesDownload.get(j).attr("src").contains("http")) {
                        String a = "imageView" + i;
                        int emptyImageId = getResources().getIdentifier(a, "id", getPackageName());
                        ImageView emptyImage = findViewById(emptyImageId);
                        insertImages(imagesDownload, i, j, emptyImage);
                        setClickTrackerUsingMainThread(emptyImage);
                        i++;
                    }
                    j++;
                }
            }
        }).start();
    }

    public void fetchButtonPressedWhileLoadingImages() {
        //get entered url before closing current activity
        EditText EnteredUrlRaw = findViewById(R.id.EnteredUrl);
        String EnteredUrl = EnteredUrlRaw.getText().toString();

        //to stop the loop in GetImagesFromUrl
        buttonPressedAgain = true;

        //redirect to "restart" activity
        Intent intent = new Intent(this, LoadImagesActivity.class);
        intent.putExtra("EnteredUrl", EnteredUrl);
        startActivity(intent);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.EnteredUrl).getWindowToken(), 0);
    }

    public void getWebsite() {
        EditText EnteredUrlRaw = findViewById(R.id.EnteredUrl);
        String EnteredUrl = EnteredUrlRaw.getText().toString();
        try {
            website = Jsoup.connect(EnteredUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClickTrackerUsingMainThread(ImageView iv) {
        runOnUiThread(() ->
                iv.setOnClickListener(view ->
                        clickImage(iv)));
    }

    public void insertImages(final Elements images, final int z, final int y,
                             final ImageView emptyImageViews) {
        runOnUiThread(() -> {
            //set images
            Picasso.get().load(images.get(y).attr("src")).fit().placeholder(R.drawable.x).into(emptyImageViews);
            emptyImageViews.setContentDescription(images.get(y).attr("src"));

            //increment progress bar and progress text
            downloadProgressBar.incrementProgressBy(1);
            if (z >= 20) {
                downloadProgressText.setText(R.string.downloadCompleted6Images);
            } else {
                downloadProgressText.setText(getString(R.string.downloadingImageProgress, z));
            }
        });
    }

    public void clickImage(ImageView iv) {
        //image not selected yet, <6 images selected
        if (!selectedImages.contains(iv) && selectedImages.size() < 6) {
            selectedImages.add(iv);
            iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.chosen));

            //6 images selected
            if (selectedImages.size() == 6) {
                proceedToSingleGame.bringToFront();
                proceedToDoubleGame.bringToFront();
                proceedToSingleGame.setOnClickListener(view -> goToSingleGame(selectedImages));
                proceedToDoubleGame.setOnClickListener(view -> goToDoubleGame(selectedImages));
            }
        }
        //image already selected
        else if (selectedImages.contains(iv)) {
            selectedImages.remove(iv);
            iv.setForeground(null);

            //image unselected results in <6 images selected
            if (selectedImages.size() < 6) {
                downloadProgressText.bringToFront();
                proceedToSingleGame.setOnClickListener(null);
                proceedToDoubleGame.setOnClickListener(null);
            }
        }
    }

    public void goToSingleGame(Collection<ImageView> selectedImages) {
        ArrayList<String> selectedImagesUrls = new ArrayList<>();
        for (ImageView image : selectedImages) {
            selectedImagesUrls.add((String) image.getContentDescription());
        }

        //set image urls in intent
        Intent intent = new Intent(this, GameActivitySinglePlayer.class);
        intent.putStringArrayListExtra("SelectedImagesUrls", selectedImagesUrls);
        startActivity(intent);
    }

    public void goToDoubleGame(Collection<ImageView> selectedImages) {
        ArrayList<String> selectedImagesUrls = new ArrayList<>();
        for (ImageView image : selectedImages) {
            selectedImagesUrls.add((String) image.getContentDescription());
        }

        //set image urls in intent
        Intent intent = new Intent(this, GameActivityDoublePlayer.class);
        intent.putStringArrayListExtra("SelectedImagesUrls2", selectedImagesUrls);
        startActivity(intent);

    }
}