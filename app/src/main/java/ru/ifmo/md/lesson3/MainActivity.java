package ru.ifmo.md.lesson3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.Void;
import java.net.URLEncoder;


public class  MainActivity extends Activity {

    private final static String translatorPrefix = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20140924T073928Z.75f4072f7ba0940a.0e25b6e1b08d1c03dfb34d22a4055d8118e44d77&lang=en-ru&text=";

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
    }

    public void onClick(View view) {
        new TranslationTask().execute(editText.getText().toString());
    }

    private class TranslationTask extends AsyncTask<String, Void, String> {
        private String word;
        @Override
        protected String doInBackground(String... toTranslate) {
            word = toTranslate[0];
            try {
                String url = translatorPrefix + URLEncoder.encode(word, "utf-8");
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
                String response = EntityUtils.toString(httpResponse.getEntity());
                JSONObject jsonObject = new JSONObject(response);
                return jsonObject.getJSONArray("text").getString(0);
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        protected void onPostExecute(String translation) {
            if(translation == null) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Ошибка")
                    .setMessage("Возникли проблемы с Интернет-соединением")
                    .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }).show();
                return;
            }
            Intent displayIntent = new Intent(getApplicationContext(), DisplayActivity.class);
            displayIntent.putExtra("translation", translation);
            displayIntent.putExtra("word", word);
            Log.i("Main", "tryin to start displayactivity");
            startActivity(displayIntent);
        }
    }
}
