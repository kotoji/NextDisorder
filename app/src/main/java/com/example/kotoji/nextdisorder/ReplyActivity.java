package com.example.kotoji.nextdisorder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kotoji.nextdisorder.R;
import com.example.kotoji.nextdisorder.TwitterUtils;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by kotoji on 25/05/2017.
 */

public class ReplyActivity extends Activity {

    private static final String SELECTED_TWEET_ID = "reply_status_navi3wr9uqbntu0";

    private Twitter _twtter;
    private EditText _input_text;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);

        _twtter = TwitterUtils.getTwitterInstance(this);
        _input_text = (EditText) findViewById(R.id.input_text);
        // BundleにTweetのIDが入ってる
        Bundle bundle = getIntent().getExtras();
        long tweet_id = bundle.getLong(SELECTED_TWEET_ID);
        setScreenName(tweet_id);

        findViewById(R.id.send_tweet_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweet();
            }
        });
    }

    private void setScreenName(Long tweet_id) {
        AsyncTask<Long, Void, String> task = new AsyncTask<Long, Void, String>() {
            @Override
            protected String doInBackground(Long... args) {
                try {
                    long tweet_id = args[0];
                    Log.d("ID__________________", new Long(tweet_id).toString());
                    twitter4j.Status tweet = _twtter.showStatus(tweet_id);
                    String screen_name = tweet.getUser().getScreenName();
                    return screen_name;
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    _input_text.setText("@" + result + " ");
                } else {
                    Log.d("DAME", "DAMEEEEEEEEEEEE");
                }
            }
        };
        task.execute(tweet_id);
    }

    private void tweet() {
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... args) {
                try {
                    long tweet_id = Long.parseLong(args[1]);
                    StatusUpdate status_update = new StatusUpdate(args[0]);
                    status_update.setInReplyToStatusId(tweet_id);
                    _twtter.updateStatus(status_update);
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
        Bundle bundle = getIntent().getExtras();
        long tweet_id = bundle.getLong(SELECTED_TWEET_ID);
        task.execute(_input_text.getText().toString(), new Long(tweet_id).toString());
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
