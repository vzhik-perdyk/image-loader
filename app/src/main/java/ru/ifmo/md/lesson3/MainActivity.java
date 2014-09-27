package ru.ifmo.md.lesson3;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.Void;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends Activity {

    private final static String translatorPrefix = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20140924T073928Z.75f4072f7ba0940a.0e25b6e1b08d1c03dfb34d22a4055d8118e44d77&lang=en-ru&text=";
    private final static String imageLoaderPrefix = "https://api.datamarket.azure.com/Bing/Search/v1/Image?Query=%27";

    private EditText editText;
    private TranslationTask translationTask;
    private ImageLoadingTask imageLoadingTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
    }

    public void onClick(View view) {
        translationTask = new TranslationTask();
        translationTask.execute(editText.getText().toString());
        try {
            imageLoadingTask = new ImageLoadingTask();
            imageLoadingTask.execute(translationTask.get());
            //get the result URLs using imageLoadingTask.get()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TranslationTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... word) {
            try {
                String url = translatorPrefix + URLEncoder.encode(word[0], "utf-8");
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                String response = EntityUtils.toString(httpResponse.getEntity());
                JSONObject jsonObject = new JSONObject(response);
                //Log.i("lal", jsonObject.getJSONArray("text").getString(0));
                return jsonObject.getJSONArray("text").getString(0);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    class ImageLoadingTask extends AsyncTask<String, Void, String[]> {
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

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (connection.getInputStream())));
                String response;
                response = br.readLine();
                connection.disconnect();
                PrintWriter writer = new PrintWriter("/sdcard/log.txt", "UTF-8");


                JSONObject jsonObject = new JSONObject(response);
                JSONArray responses = jsonObject.getJSONObject("d").getJSONArray("results");
                String[] results = new String[10];
                for (int i = 0; i < responses.length() && i < 10; i++) {
                    results[i] = responses.getJSONObject(i).getString("MediaUrl");
                }
                for (int i = 0; i < results.length; i++) {
                    Log.i("Response", "URL = " + results[i]);
                }
                writer.println(response);
                return results;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
