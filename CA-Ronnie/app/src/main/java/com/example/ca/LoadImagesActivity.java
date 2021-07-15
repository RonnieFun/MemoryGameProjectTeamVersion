package com.example.ca;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
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

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadImagesActivity extends AppCompatActivity {

    private Elements elements;
    private Document document;
    private ProgressBar downloadProgressBar;
    protected Thread bkgdThread;
    private TextView downloadProgressText;
    private TextView proceedToSingleGame;
    private TextView proceedToDoubleGame;
    private EditText enteredUrl;
    private List<String> srcList = new ArrayList<>();
    private final Collection<ImageView> selectedImages = new ArrayList<>();
    int numberOfColumns = 4;

    //changeable variables
    int numberOfImages = 30;
    int numberOfGameImages = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadimages);

        Button getUrl = findViewById(R.id.FetchButton);
        getUrl.setOnClickListener((view) -> GetImagesFromUrl());

        downloadProgressText = findViewById(R.id.DownloadProgress);
        enteredUrl = findViewById(R.id.EnteredUrl);
        proceedToSingleGame = findViewById(R.id.ProceedToSinglePlayerGame);
        proceedToDoubleGame = findViewById(R.id.ProceedToDoublePlayerGame);
        downloadProgressBar = findViewById(R.id.DownloadProgressBar);
        proceedToSingleGame = findViewById(R.id.ProceedToSinglePlayerGame);
        proceedToDoubleGame = findViewById(R.id.ProceedToDoublePlayerGame);
        getUrl.bringToFront();
    }

    public void GetImagesFromUrl() {
        //check the user enter url and press fetch while downloading
        if (bkgdThread != null) {
            bkgdThread.interrupt();
            downloadProgressBar.setVisibility(View.INVISIBLE);
            proceedToSingleGame.setVisibility(View.INVISIBLE);
            proceedToDoubleGame.setVisibility(View.INVISIBLE);
            downloadProgressBar.setProgress(0);
            downloadProgressText.setVisibility(View.VISIBLE);
            downloadProgressText.setText("Checking the website...");
            selectedImages.clear();
            ConstraintLayout loadImagesViewImages = findViewById(R.id.Images);
            for (int i = 0; i < loadImagesViewImages.getChildCount(); i++) {
                Glide.with(this).load(R.drawable.x).into((ImageView) loadImagesViewImages.getChildAt(i));
                loadImagesViewImages.getChildAt(i).setForeground(null);
            }
        }

        downloadProgressBar.setVisibility(View.VISIBLE);
        bkgdThread = new Thread() {
            @Override
            public void run() {
                super.run();
                if (bkgdThread.isInterrupted()) {
                    return;
                }
                //set up progress bar and status
                downloadProgressText.setText("Checking the website...");
                hideKeyboard();
                //download data from url
                downloadData();
                //bind data in UI
                bindDataInUI();
            }
        };
        bkgdThread.start();
    }

    private void bindDataInUI() {
        int i = 1;
        if (srcList.size() > 0) {
            for (String src : srcList) {
                String a = "imageView" + i;
                Bitmap bitmap = null;
                int emptyImageId = getResources().getIdentifier(a, "id", getPackageName());
                ImageView emptyImage = findViewById(emptyImageId);
                try {
                    bitmap = Glide.with(getBaseContext()).asBitmap().load(src).submit().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Bitmap finalBitmap = bitmap;
                runOnUiThread(() -> {
                    //insertImages(imagesDownload, i, j, emptyImage,bitmap);
                    emptyImage.setImageBitmap(finalBitmap);
                    emptyImage.setContentDescription(src);
                    //increment progress bar and progress text
                    downloadProgressBar.incrementProgressBy(1);
                });
                if (i >= numberOfImages) {
                    downloadProgressText.setText(getString(R.string.downloadCompletedXImages, numberOfGameImages));
                } else {
                    downloadProgressText.setText(getString(R.string.downloadingImageProgress, i));
                }
                setClickTrackerUsingMainThread(emptyImage);
                i++;
            }
        }

    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.EnteredUrl).getWindowToken(), 0);
    }

    public void downloadData() {
        String EnteredUrl = enteredUrl.getText().toString();
        document = null;
        elements = null;
        srcList.clear();
        try {
            int index = 0;
            document = Jsoup.connect(EnteredUrl).get();
            elements = document.getElementsByTag("img");
            for (Element element : elements) {
                String imgSrc = element.attr("src");
                if (imgSrc.contains(".jpg") || imgSrc.contains(".png")) {
                    if (index >= 20) {
                        break;
                    } else {
                        srcList.add(imgSrc);
                        index++;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClickTrackerUsingMainThread(ImageView iv) {
        runOnUiThread(() ->
                iv.setOnClickListener(view ->
                        clickImage(iv)));
    }

    public void clickImage(ImageView iv) {
        //image not selected yet, <6 images selected
        if (!selectedImages.contains(iv) && selectedImages.size() < 6) {
            selectedImages.add(iv);
            iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.tick));
            //6 images selected
            if (selectedImages.size() == 6) {
                downloadProgressBar.setVisibility(View.INVISIBLE);
                downloadProgressText.setVisibility(View.INVISIBLE);
                proceedToSingleGame.setVisibility(View.VISIBLE);
                proceedToDoubleGame.setVisibility(View.VISIBLE);
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