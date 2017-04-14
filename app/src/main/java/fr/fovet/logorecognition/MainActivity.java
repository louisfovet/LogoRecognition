package fr.fovet.logorecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private Uri destination;
    private String filePath;

    private AssetManager assetManager;
    private CallServer callServer;

    /**
     * A la création de l'activité, appelle le serveur afin de récupérer les informations
     * nécessaires à l'application de l'algo SIFT.
     * On a 3 boutons sur cette activité:
     * - prise de photo
     * - sélection de photo dans la bibliothèque
     * - analyse de la photo (lancement d'AnalysisActivity)
     */
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

        //assetManager = new AssetManager(this);
        callServer = new CallServer(this);

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
                try {
                    /*Mat descriptor = Utils.getDescriptor(getApplicationContext(), filePath);
                    String match = assetManager.searchForMatch(descriptor);*/

                    Compute c = new Compute(getApplicationContext(), callServer, filePath);

                    Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
                    intent.putExtra("MATCH", c.getBestMatch());
                    intent.putExtra("IMAGE", c.getImagePathMatch());
                    intent.putExtra("URL", c.getUrlMatch());
                    Log.i("MainActivity", "onActivityResult: match " + c.getBestMatch());
                    startActivity(intent);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 3 résultats possibles:
     * - traitement si on a capturé une photo
     * - traitement si on a sélectionné une photo
     * - traitement si on a crop la photo
     * Dans tous les cas, la photo affichée est actualisée
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null) {

            Bitmap bitmap = null;

            if(requestCode == REQUEST_IMAGE_CAPTURE) {
                bitmap = BitmapFactory.decodeFile(photoURI.getPath());
            }

            if(requestCode == REQUEST_IMAGE_SELECT) {
                photoURI = data.getData();

                try {
                    InputStream is = getContentResolver().openInputStream(photoURI);
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            if(requestCode == Crop.REQUEST_CROP) {
                photoURI = Crop.getOutput(data);
                bitmap = BitmapFactory.decodeFile(photoURI.getPath());
            }


            Bitmap resizedBitmap = Utils.scaleBitmapDown(bitmap, 500);
            filePath = Utils.BitmapToCache(this, resizedBitmap, "test.jpg").getPath();

            imgAnalysis.setImageURI(photoURI);
            btnAnalysis.setEnabled(true);
            btnAnalysis.setText("Analysis");


            if(requestCode != Crop.REQUEST_CROP) {
                destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
                Crop.of(photoURI, destination).start(this);
            }

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

    /**
     * Création d'un fichier
     *
     * @return  File    fichier dans lequel sera stocké l'image capturé
     */
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

    /**
     * Intent utilisé pour la capture d'une photo
     */
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
                photoURI = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Intent utilisé pour la sélection d'une photo
     */
    private void dispatchSelectPictureIntent() {
        /*Intent selectPictureIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(selectPictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(selectPictureIntent, REQUEST_IMAGE_SELECT);
        }*/
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), REQUEST_IMAGE_SELECT);
    }

}
