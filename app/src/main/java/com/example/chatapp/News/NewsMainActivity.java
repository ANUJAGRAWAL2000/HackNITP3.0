package com.example.chatapp.News;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.chatapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class NewsMainActivity extends AppCompatActivity {

    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> articleUrl = new ArrayList<>();
    SQLiteDatabase articleDB;
    ArrayAdapter adapter;
    private ProgressDialog dialog;
    final int TIME = 10000 * 60 * 60 * 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_main);

        articleDB = this.openOrCreateDatabase("Articles", MODE_PRIVATE, null);
        articleDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleId INTEGER, title VARCHAR, articleUrl VARCHAR)");

        ListView listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, title);
        listView.setAdapter(adapter);

        try {
                            DownloadTask downloadTask = new DownloadTask();
                            Log.i("Downloading.....", "Downloading now");

                            downloadTask.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty").get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
                intent.putExtra("articleUrl", articleUrl.get(i));
                startActivity(intent);

            }
        });

        updateListView();

    }

    public void updateListView() {

        Cursor c = articleDB.rawQuery("SELECT * FROM articles", null);

        int urlIndex = c.getColumnIndex("articleUrl");
        int titleIndex = c.getColumnIndex("title");

        if(c.moveToFirst()) {

            title.clear();
            articleUrl.clear();

            do{

                title.add(c.getString(titleIndex));
                articleUrl.add(c.getString(urlIndex));

            } while (c.moveToNext());

            adapter.notifyDataSetChanged();

        }

    }

    public class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = null;
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }

                JSONArray jsonArray = new JSONArray(result);
                int numberOfItems = 20;
                if (jsonArray.length() < 20) {

                    numberOfItems = jsonArray.length();

                }

                articleDB.execSQL("DELETE FROM articles");

                for (int i = 0; i < numberOfItems; i++) {

                    publishProgress(i);
                    String articleId = jsonArray.getString(i);

                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" + articleId + ".json?print=pretty");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    inputStream = connection.getInputStream();
                    reader = new InputStreamReader(inputStream);

                    data = reader.read();

                    String articleInfo = "";

                    while (data != -1) {

                        char current = (char) data;
                        articleInfo += current;
                        data = reader.read();

                    }

                    JSONObject jsonObject = new JSONObject(articleInfo);
                    if(!jsonObject.isNull("title") && !jsonObject.isNull("url")) {

                        String articleTitle = jsonObject.getString("title");
                        String articleUrl = jsonObject.getString("url");

                        String sql = "INSERT INTO articles (articleId, title, articleUrl) VALUES(?, ?, ?)";
                        SQLiteStatement statement = articleDB.compileStatement(sql);
                        statement.bindString(1, articleId);
                        statement.bindString(2, articleTitle);
                        statement.bindString(3, articleUrl);
                        statement.execute();

                    }
                }

            } catch (Exception e) {

                e.printStackTrace();

            }

            return null;
        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            dialog = new ProgressDialog(NewsMainActivity.this);
            dialog.setMessage("Loading...");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            Log.i("IS showin?", String.valueOf(dialog.isShowing()));
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            dialog.dismiss();
            Log.i("Running post.....", String.valueOf(dialog.isShowing()));
            updateListView();

        }
    }
}