package ru.ifmo.md.lesson3;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Лоскутов Игнат (2538), Некрасов Дмитрий (2538), Шолохов Алексей (2536)
 */
public class DisplayActivity extends Activity {

    private final static String imageLoaderPrefix = "https://api.datamarket.azure.com/Bing/Search/v1/Image?Query=%27";
    private static int imagesDrawn = 0;
    private GridLayout layout;
    private GetImageTask getImageTask;
    private ImageLoadingTask imageLoadingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_activity);
        layout = (GridLayout)findViewById(R.id.display_activity);
        Intent thisIntent = getIntent();
        String translation = thisIntent.getStringExtra("translation");
        String word = thisIntent.getStringExtra("word");
        TextView translationView = (TextView)findViewById(R.id.translationView);
        translationView.setText(translation);
        imageLoadingTask = new ImageLoadingTask();
        imageLoadingTask.execute(word);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(imageLoadingTask != null) {
            imageLoadingTask.cancel(true);
        }
        if(getImageTask != null) {
            getImageTask.cancel(true);
        }
        Log.i("DisplayActivity", "destroyed((");
    }

    // Get images from Bing
    /*private class ImageLoadingTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... word) {
            try {
                URL url = new URL(imageLoaderPrefix + URLEncoder.encode(word[0], "utf-8") + "%27");
                Log.i("url", url.toString());
                String accountKey = ":Vik8blAkuBkO6VyX8qJV+74lq79nNifdTToND5N8pQo=";
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                byte[] accountKeyBytes = accountKey.getBytes("utf-8");
                String accountKeyEnc = Base64.encodeToString(accountKeyBytes, Base64.DEFAULT);
                connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response;
                response = br.readLine();
                connection.disconnect();

                JSONObject jsonObject = new JSONObject(response);
                JSONArray responses = jsonObject.getJSONObject("d").getJSONArray("results");
                int resultsLength = Math.min(responses.length(), 10);
                String[] results = new String[resultsLength];
                for (int i = 0; i < resultsLength; i++) {
                    results[i] = responses.getJSONObject(i).getString("MediaUrl");
                }
                for (String result : results) {
                    Log.i("Response", "URL = " + result);
                }
                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }*/

    //Get images from Flickr
    private class ImageLoadingTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... word) {
            if(isCancelled())
                return null;

            try {
                String url = ("https://api.flickr.com/services/rest/?method=flickr.photos.search&per_page=10&format=json&api_key=5c7c3f396db79009d2baecd139398d98&text=" + URLEncoder.encode(word[0], "utf-8"));
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                String response = EntityUtils.toString(httpResponse.getEntity());
                response = response.substring(response.indexOf('{'), response.lastIndexOf(')'));
                JSONObject jsonObject = new JSONObject(response);

                JSONArray responses = jsonObject.getJSONObject("photos").getJSONArray("photo");
                String[] results = new String[responses.length()];
                for (int i = 0; i < results.length; i++) {
                    JSONObject picture = responses.getJSONObject(i);
                    results[i] = String.format("https://farm%d.staticflickr.com/%s/%s_%s_z.jpg",
                            picture.getInt("farm"),
                            picture.getString("server"),
                            picture.getString("id"),
                            picture.getString("secret")
                    );
                }
                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
        protected void onPostExecute(String[] imageUrls) {
            if(imageUrls != null) {
                for (String imageUrl : imageUrls) {
                    getImageTask = new GetImageTask();
                    getImageTask.execute(imageUrl);
                }
            }
        }
    }

    private class GetImageTask extends AsyncTask<String, Void, Drawable> {
        @Override
        protected Drawable doInBackground(String... word) {
            if(isCancelled())
                return null;

            try {
                Log.i("lal", "tryin");
                InputStream is = (InputStream) new URL(word[0]).getContent();
                Drawable img = Drawable.createFromStream(is, "");
                is.close();
                return img;
            } catch (Exception e) {
                Log.i("lal", "i tried");
                return null;
            }
        }

        protected void onPostExecute(Drawable d) {
            ImageView imgView = new ImageView(getApplicationContext());
            imgView.setScaleType(ImageView.ScaleType.CENTER);
            imgView.setImageDrawable(d);
            GridLayout.Spec rowSpec = GridLayout.spec(imagesDrawn / 2, GridLayout.CENTER);
            GridLayout.Spec colSpec = GridLayout.spec(imagesDrawn % 2, GridLayout.CENTER);
            layout.addView(imgView, new GridLayout.LayoutParams(rowSpec, colSpec));
            imagesDrawn++;
        }
    }
}
