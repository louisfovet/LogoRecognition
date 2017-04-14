package fr.fovet.logorecognition;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class AnalysisActivity extends AppCompatActivity {

    private Button btnWebsite;
    private ImageView imgLogo;

    private String match;
    private String image;
    private String url;

    /**
     * A la création de l'activité, on récupère la marque qui a match avec notre photo
     * Le nom et l'image de référence de la marque est affichée
     * Le site web est disponible via un boutton.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnWebsite = (Button) findViewById(R.id.btnWebsite);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);

        Intent intent = getIntent();
        match = intent.getStringExtra("MATCH");
        image = intent.getStringExtra("IMAGE");
        url = intent.getStringExtra("URL");

        //website = Utils.getWebsite(match);

        /*String filePath = Utils.AssetToCache(this, "images" + "/" + match, match).getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);*/

        imgLogo.setImageBitmap(BitmapFactory.decodeFile(image));
        btnWebsite.setText(match + " - GO TO WEBSITE");

        btnWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(url));
                startActivity(browserIntent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
