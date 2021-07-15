package com.example.ca;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class LoadImagesActivity2 extends AppCompatActivity {

    private Elements imagesDownload;
    private Document website;
    private ProgressBar downloadProgressBar;
    private TextView downloadProgressText;
    private TextView proceedToGame;
    private final Collection<ImageView> selectedImages = new ArrayList<>();
    private boolean buttonPressedAgain = false;
    int numberOfColumns = 5;

    //changeable variables
    int numberOfImages = 30;
    int numberOfGameImages = 10;

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
        proceedToGame = findViewById(R.id.ProceedToGame);

        getUrl.bringToFront();
        downloadProgressText.setText(R.string.awaitingUrlInput);
        downloadProgressText.bringToFront();

        if (intent.getBooleanExtra("startProgram", false)) {
            GetImagesFromUrl();
        }
    }

    public void GetImagesFromUrl() {
        //set up alternate Fetch button
        Button getUrlWhileScraping = findViewById(R.id.FetchButtonWhileScraping);
        getUrlWhileScraping.setOnClickListener((view) -> fetchButtonPressedWhileLoadingImages(true));
        getUrlWhileScraping.bringToFront();

        //scrape website and get images on new thread so main thread can load progress
        new Thread(() -> {
            try {
                //set up progress bar and status
                LinearLayout images = findViewById(R.id.Images);
                downloadProgressBar = findViewById(R.id.DownloadProgressBar);
                downloadProgressText.setText(getString(R.string.checkingTheWebsite));
                downloadProgressBar.setMax(numberOfImages);

                for (int a = 0; a < Math.ceil((double) numberOfImages / (double) numberOfColumns); a++) {
                    LinearLayout linearLayout = new LinearLayout(this);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams lpForRows = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    linearLayout.setLayoutParams(lpForRows);

                    for (int b = 0; b < numberOfColumns; b++) {
                        ImageView imageView = new ImageView(this);
                        LinearLayout.LayoutParams lpForImages = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                        lpForImages.weight = 1;
                        lpForImages.height = (int) ((this.getResources().getDisplayMetrics().heightPixels) * 0.14);
                        imageView.setLayoutParams(lpForImages);
                        linearLayout.addView(imageView);
                    }
                    runOnUiThread(() -> images.addView(linearLayout));
                }

                //load placeholder images
                LinearLayout allEmptyImageViews = findViewById(R.id.Images);
                for (int i = 0; i < allEmptyImageViews.getChildCount(); i++) {
                    LinearLayout imageRows = (LinearLayout) allEmptyImageViews.getChildAt(i);
                    for (int j = 0; j < imageRows.getChildCount(); j++) {
                        ImageView child = (ImageView) imageRows.getChildAt(j);
                        child.setEnabled(false);
                    }
                }

                hideKeyboard();
                getWebsite();

                //get images from website
                imagesDownload = website.getElementsByTag("img");
                int i = 0;
                int j = 0;
                int y = 0;
                int x = 0;
                while (i < numberOfImages && !buttonPressedAgain) {
                    if (imagesDownload.get(j).attr("src") != null && imagesDownload.get(j).attr("src").contains("http")) {
                        LinearLayout ll = (LinearLayout) images.getChildAt(y);
                        ImageView emptyImage = (ImageView) ll.getChildAt(x);
                        insertImages(imagesDownload, i, j, emptyImage);
                        setClickTrackerUsingMainThread(emptyImage);
                        i++;
                        x++;
                        if (x >= numberOfColumns) {
                            y++;
                            x = 0;
                        }
                    }
                    j++;
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "We were unable to extract the images from the entered url. Please enter another url.", Toast.LENGTH_LONG).show());
                fetchButtonPressedWhileLoadingImages(false);
            }
        }).

                start();

    }

    public void fetchButtonPressedWhileLoadingImages(boolean startProgram) {
        //get entered url before closing current activity
        EditText EnteredUrlRaw = findViewById(R.id.EnteredUrl);
        String EnteredUrl = EnteredUrlRaw.getText().toString();

        //to stop the loop in GetImagesFromUrl
        buttonPressedAgain = true;

        //redirect to "restart" activity
        Intent intent = new Intent(this, LoadImagesActivity2.class);
        intent.putExtra("EnteredUrl", EnteredUrl);
        intent.putExtra("startProgram", startProgram);
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

    public void insertImages(Elements images, int i, int j,
                             ImageView emptyImageViews) {
        runOnUiThread(() -> {
            //set images
            Picasso.get().load(images.get(j).attr("src")).fit().placeholder(R.drawable.x).into(emptyImageViews);
            emptyImageViews.setContentDescription(images.get(j).attr("src"));

            //increment progress bar and progress text
            downloadProgressBar.incrementProgressBy(1);
            if (i + 1 >= numberOfImages) {
                downloadProgressText.setText(getString(R.string.downloadCompletedImages, numberOfGameImages));
            } else {
                downloadProgressText.setText(getString(R.string.downloadingImageProgress, i + 1, numberOfImages));
            }
        });
    }

    public void clickImage(ImageView iv) {
        //image not selected yet, <6 images selected
        if (!selectedImages.contains(iv) && selectedImages.size() < numberOfGameImages) {
            selectedImages.add(iv);
            downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size(), numberOfGameImages));
            iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.chosen));

            //6 images selected
            if (selectedImages.size() == numberOfGameImages) {
                proceedToGame.bringToFront();
                proceedToGame.setOnClickListener(view -> goToGame(selectedImages));
            }
        }
        //image already selected
        else if (selectedImages.contains(iv)) {
            selectedImages.remove(iv);
            downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size(), numberOfGameImages));
            iv.setForeground(null);

            //image unselected results in <6 images selected
            if (selectedImages.size() < numberOfGameImages) {
                downloadProgressText.bringToFront();
                downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size(), numberOfGameImages));
                proceedToGame.setOnClickListener(null);
            }
        }
    }

    public void goToGame(Collection<ImageView> selectedImages) {
        ArrayList<String> selectedImagesUrls = new ArrayList<>();
        for (ImageView image : selectedImages) {
            selectedImagesUrls.add((String) image.getContentDescription());
        }

        //set image urls in intent
        Intent intent = new Intent(this, GameActivity2.class);
        intent.putStringArrayListExtra("SelectedImagesUrls", selectedImagesUrls);
        intent.putExtra("numberOfGameImages", numberOfGameImages);
        startActivity(intent);
    }
}