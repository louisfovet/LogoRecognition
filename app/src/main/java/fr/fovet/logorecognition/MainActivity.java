package fr.fovet.logorecognition;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_calib3d;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacpp.opencv_features2d.*;
import org.bytedeco.javacpp.opencv_nonfree.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_SELECT = 2;

    private Button btnCapture;
    private Button btnLibrary;
    private Button btnAnalysis;
    private ImageView imgAnalysis;

    private Uri photoURI;
    private String photoPath;
    private String filePath;

    private AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnCapture = (Button) findViewById(R.id.btnCapture);
        btnLibrary = (Button) findViewById(R.id.btnLibrary);
        btnAnalysis = (Button) findViewById(R.id.btnAnalysis);
        imgAnalysis = (ImageView) findViewById(R.id.imgAnalysis);

        btnAnalysis.setEnabled(true);

        assetManager = new AssetManager(this);

        String refFile = "starbucks0.png";
        filePath = Utils.AssetToCache(this, "images" + "/" + refFile, refFile).getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imgAnalysis.setImageBitmap(bitmap);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchSelectPictureIntent();
            }
        });

        btnAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
                //startActivity(intent);
                try {

                    Mat descriptor = new Mat();
                    descriptor = Utils.getDescriptor(getApplicationContext(), filePath);
                    assetManager.searchForMatch(descriptor);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null) {

            if(requestCode == REQUEST_IMAGE_CAPTURE) {

            }

            if(requestCode == REQUEST_IMAGE_SELECT) {
                photoURI = data.getData();
            }

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Bitmap resizedBitmap = Utils.scaleBitmapDown(bitmap, 500);
            filePath = Utils.BitmapToCache(this, resizedBitmap, "test.jpg").getPath();

            imgAnalysis.setImageURI(photoURI);
            btnAnalysis.setEnabled(true);
            btnAnalysis.setText("Analysis");

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                timeStamp,
                ".jpg",
                storageDir
        );
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                photoPath = photoFile.getAbsolutePath();
                //photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void dispatchSelectPictureIntent() {
        Intent selectPictureIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(selectPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(selectPictureIntent, REQUEST_IMAGE_SELECT);
        }
    }





}
