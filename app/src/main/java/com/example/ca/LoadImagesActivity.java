package com.example.ca;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LoadImagesActivity extends AppCompatActivity {

    private ProgressBar downloadProgressBar;
    protected Thread backgroundThread;
    private TextView downloadProgressText;
    private TextView proceedToSingleGame;
    private TextView proceedToDoubleGame;
    private LinearLayout allImages;
    private EditText enteredUrl;
    private final List<String> srcList = new ArrayList<>();
    private final Collection<ImageView> selectedImages = new ArrayList<>();
    int numberOfColumns = 4;
    int numberOfRows;

    //changeable variables
    int numberOfImages = 20;
    int numberOfGameImages = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setUpView();
    }

    public void setUpView() {
        setContentView(R.layout.loadimages);

        Button getUrl = findViewById(R.id.FetchButton);
        getUrl.setOnClickListener((view) -> getImagesFromUrl());

        downloadProgressText = findViewById(R.id.DownloadProgress);
        enteredUrl = findViewById(R.id.EnteredUrl);
        proceedToSingleGame = findViewById(R.id.ProceedToSinglePlayerGame);
        proceedToDoubleGame = findViewById(R.id.ProceedToDoublePlayerGame);
        downloadProgressBar = findViewById(R.id.DownloadProgressBar);
        downloadProgressBar.setMax(numberOfImages);
        proceedToSingleGame = findViewById(R.id.ProceedToSinglePlayerGame);
        proceedToDoubleGame = findViewById(R.id.ProceedToDoublePlayerGame);
        allImages = findViewById(R.id.AllImages);
        numberOfRows = (int) Math.ceil((double) numberOfImages / (double) numberOfColumns);
        getUrl.bringToFront();
        createEmptyImageViews();
    }

    public void createEmptyImageViews() {
        int i = 0;
        for (int a = 0; a < numberOfRows; a++) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lpForRows = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(lpForRows);

            for (int b = 0; b < numberOfColumns && i < numberOfImages; b++) {
                ImageView imageView = new ImageView(this);
                LinearLayout.LayoutParams lpForImages = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
                lpForImages.height = (int) ((this.getResources().getDisplayMetrics().heightPixels) * 0.12);
                lpForImages.width = (this.getResources().getDisplayMetrics().widthPixels) / 4;

                //set 5dp margin around images
                //float margin = 5 * getResources().getDisplayMetrics().density;
                //lpForImages.setMargins((int) margin, (int) margin, (int) margin, (int) margin);
                imageView.setLayoutParams(lpForImages);
                imageView.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.x));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                linearLayout.addView(imageView);
                i++;
            }
            allImages.addView(linearLayout);
        }
    }

    public void getImagesFromUrl() {
        //check the user enter url and press fetch while downloading
        if (backgroundThread != null) {
            backgroundThread.interrupt();
            downloadProgressBar.setVisibility(View.INVISIBLE);
            proceedToSingleGame.setVisibility(View.INVISIBLE);
            proceedToDoubleGame.setVisibility(View.INVISIBLE);
            downloadProgressBar.setProgress(0);
            downloadProgressText.setVisibility(View.VISIBLE);
            downloadProgressText.setText(R.string.checkingWebsite);
            selectedImages.clear();
            for (int a = 0; a < numberOfRows; a++) {
                LinearLayout linearLayout = (LinearLayout) allImages.getChildAt(a);
                for (int b = 0; b < numberOfColumns; b++) {
                    ImageView imageView = (ImageView) linearLayout.getChildAt(b);
                    Glide.with(this).load(R.drawable.x).into(imageView);
                    imageView.setForeground(null);
                }
            }
        }
        downloadProgressBar.setVisibility(View.VISIBLE);
        backgroundThread = new Thread() {
            @Override
            public void run() {
                super.run();
                //set up progress bar and status
                downloadProgressText.setText(R.string.checkingWebsite);
                hideKeyboard();
                //download data from url
                parseUrl();
                //bind data in UI
                bindDataInUI();
            }
        };
        backgroundThread.start();
    }

    private void bindDataInUI() {
        int i = 0;
        int y = 0;
        int x = 0;
        Bitmap bitmap = null;

        if (srcList.size() > 0) {
            for (String src : srcList) {
                while (y < numberOfRows) {
                    LinearLayout imageRows = (LinearLayout) allImages.getChildAt(y);
                    while (x < numberOfColumns) {
                        if (backgroundThread.isInterrupted()) {
                            return;
                        }
                        ImageView emptyImageView = (ImageView) imageRows.getChildAt(x);
                        try {
                            bitmap = Glide.with(getBaseContext()).asBitmap().load(src).submit().get();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Bitmap finalBitmap = bitmap;
                        runOnUiThread(() -> {
                            emptyImageView.setImageBitmap(finalBitmap);
                            emptyImageView.setContentDescription(src);
                            downloadProgressBar.incrementProgressBy(1);
                        });
                        if (i >= numberOfImages - 1) {
                            downloadProgressText.setText(getString(R.string.downloadCompletedXImages, numberOfGameImages));
                        } else {
                            downloadProgressText.setText(getString(R.string.downloadingImageProgress, i + 1, numberOfImages));
                        }
                        x++;
                        if (x >= numberOfColumns) {
                            y++;
                            x = 0;
                        }
                        break;
                    }
                    i++;
                    break;
                }
            }
        }
        setAllImagesClickable();
    }

    public void setAllImagesClickable() {
        int i = 0;
        for (int a = 0; a < numberOfRows; a++) {
            LinearLayout linearLayout = (LinearLayout) allImages.getChildAt(a);
            for (int b = 0; b < numberOfColumns && i < numberOfImages; b++) {
                ImageView imageView = (ImageView) linearLayout.getChildAt(b);
                runOnUiThread(() -> imageView.setOnClickListener(view -> clickImage(imageView)));
                i++;
            }
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.EnteredUrl).getWindowToken(), 0);
    }

    public void parseUrl() {
        String EnteredUrl = enteredUrl.getText().toString();
        Document document;
        Elements elements;
        srcList.clear();
        try {
            int index = 0;
            document = Jsoup.connect(EnteredUrl).get();
            elements = document.getElementsByTag("img");
            for (Element element : elements) {
                String imgSrc = element.attr("src");
                if (imgSrc.contains(".jpg") || imgSrc.contains(".png")) {
                    if (index >= numberOfImages) {
                        break;
                    } else {
                        srcList.add(imgSrc);
                        index++;
                    }
                }
            }
        } catch (Exception e) {
            runOnUiThread(() -> {
                Toast toast = Toast.makeText(this, "No images found. Please enter another url.", Toast.LENGTH_LONG);
                TextView toastMessage = (TextView) toast.getView().findViewById(android.R.id.message);
                toastMessage.setTextColor(Color.RED);
                toast.show();
                e.printStackTrace();

                setUpView();
            });
        }
    }

    public void clickImage(ImageView iv) {
        //image not selected yet, <6 images selected
        if (!selectedImages.contains(iv) && selectedImages.size() < numberOfGameImages) {
            selectedImages.add(iv);
            downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size(), numberOfGameImages));
            iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.tick));
            //6 images selected
            if (selectedImages.size() == numberOfGameImages) {
                downloadProgressBar.setVisibility(View.INVISIBLE);
                downloadProgressText.setVisibility(View.INVISIBLE);
                proceedToSingleGame.setVisibility(View.VISIBLE);
                proceedToDoubleGame.setVisibility(View.VISIBLE);
                proceedToSingleGame.setOnClickListener(view -> goToSingleGame(selectedImages));
                proceedToDoubleGame.setOnClickListener(view -> goToDoubleGame(selectedImages));
            }
        }
        //image already selected
        else if (selectedImages.contains(iv)) {
            selectedImages.remove(iv);
            downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size(), numberOfGameImages));
            iv.setForeground(null);

            //image unselected results in <6 images selected
            if (selectedImages.size() < numberOfGameImages) {
                downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size(), numberOfGameImages));
                downloadProgressBar.setVisibility(View.VISIBLE);
                downloadProgressText.setVisibility(View.VISIBLE);
                proceedToSingleGame.setVisibility(View.INVISIBLE);
                proceedToDoubleGame.setVisibility(View.INVISIBLE);
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
        intent.putExtra("numberOfGameImages", numberOfGameImages);
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
        intent.putExtra("numberOfGameImages", numberOfGameImages);
        startActivity(intent);
    }
}
