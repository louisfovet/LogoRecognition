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
    String website;

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
        String match = intent.getStringExtra("MATCH");
        website = Utils.getWebsite(match);

        String filePath = Utils.AssetToCache(this, "images" + "/" + match, match).getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        imgLogo.setImageBitmap(bitmap);
        btnWebsite.setText(match.split("0")[0] + " - GO TO WEBSITE");

        btnWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(website));
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
