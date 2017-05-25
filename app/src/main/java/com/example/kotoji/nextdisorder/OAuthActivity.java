package com.example.kotoji.nextdisorder;

/**
 * Created by kotoji on 13/09/2012.
 */

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class OAuthActivity extends Activity {

    private String _callback_url;
    private Twitter _twitter;
    private RequestToken _request_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);

        _callback_url = getString(R.string.callback_url);
        _twitter = TwitterUtils.getTwitterInstance(this);

        findViewById(R.id.oauth_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAuthorize();
            }
        });
    }

    // OAuth認証をする
    // Step1 認証画面のURLを取得してページを表示
    private void startAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    _request_token = _twitter.getOAuthRequestToken(_callback_url);
                    return _request_token.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                // だめだった
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    showToast("認証に失敗しました");
                }
            }
        };
        task.execute();
    }

    // Step2 AuthorizationURLが返ってくるのでそれ使ってアクセストークン取得
    // AuthorizationURLは nekoneko://nyaaan?oauth_token=hogehoge&oauth_verifier=hugahuga の形をしている
    @Override
    public void onNewIntent(Intent intent) {
        // 上のインテントじゃなければ無視
        if (intent == null || intent.getData() == null ||
                !intent.getData().toString().startsWith(_callback_url)) {
            return;
        }

        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... strings) {
                try {
                    return _twitter.getOAuthAccessToken(_request_token, strings[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken access_token) {
                if (access_token != null) {
                    showToast("認証成功");
                    successOAuth(access_token);
                } else {
                    showToast("認証に失敗しました");
                }
            }
        };
        task.execute(verifier);
    }
    // 認証ここまで

    // 上で認証に成功したら呼ばれる
    // アクセストークンを保存してメインアクティビティに移動
    private void successOAuth(AccessToken access_token) {
        TwitterUtils.storeAccessToken(this, access_token);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}