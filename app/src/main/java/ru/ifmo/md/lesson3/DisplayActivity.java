package ru.ifmo.md.lesson3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;


/**
 * Created by ignat on 27.09.14.
 */
public class DisplayActivity extends Activity {

    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_activity);
        Intent thisIntent = getIntent();
        String[] imageUrls = thisIntent.getStringArrayExtra("imageUrls");
        String translation = thisIntent.getStringExtra("translation");
        TextView translationView = (TextView)findViewById(R.id.translationView);
        translationView.setText(translation);
         ll = (LinearLayout)findViewById(R.id.display_activity);
        for (int i = 0; i<10; i++) {
            GetImageTask getimagetask = new GetImageTask();
            getimagetask.execute(imageUrls[i]);
        }

    }

    class GetImageTask extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... word) {
            try {
                InputStream is = (InputStream)new URL(word[0]).getContent();
                Drawable img = Drawable.createFromStream(is, "");
                return img;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(Drawable d) {
            ImageView imgView = new ImageView(getApplicationContext());
            imgView.setImageDrawable(d);
            ll.addView(imgView);
        }
    }



}
