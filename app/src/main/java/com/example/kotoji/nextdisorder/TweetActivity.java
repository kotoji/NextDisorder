package com.example.kotoji.nextdisorder;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by kotoji on 15/09/2012.
 */

public class TweetActivity extends Activity {

    private Twitter _twtter;
    private EditText _input_text;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        _twtter = TwitterUtils.getTwitterInstance(this);
        _input_text = (EditText) findViewById(R.id.input_text);

        findViewById(R.id.send_tweet_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweet();
            }
        });
    }

    private void tweet() {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... strings) {
                try {
                    _twtter.updateStatus(strings[0]);
                    return true;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    showToast("Success Tweet");
                    finish();
                } else {
                    showToast("Failed Tweet");
                }
            }
        };
        task.execute(_input_text.getText().toString());
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
