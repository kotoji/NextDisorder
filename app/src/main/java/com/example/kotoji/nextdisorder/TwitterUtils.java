package com.example.kotoji.nextdisorder;

/**
 * Created by kotoji on 13/09/2012.
 */

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TwitterUtils {
    private static final String TOKEN = "token_aetbdfbwas";
    private static final String TOKEN_SECRET = "token_secret_fio3wnrinzfva";
    private static final String PREF = "pref_twitter_banennagnayn";

    // Twitterクラスのインスタンスを返す
    // すでにアクセストークンがプレフにあればセットしておく
    public static Twitter getTwitterInstance(Context context) {
        String cons_key = context.getString(R.string.twitter_consumer_key);
        String cons_sec = context.getString(R.string.twitter_consumer_secret);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setOAuthConsumerKey(cons_key)
                .setOAuthConsumerSecret(cons_sec)
                .setOAuthAccessToken(null)
                .setOAuthAccessTokenSecret(null);
        TwitterFactory factory = new TwitterFactory(builder.build());
        Twitter twitter = factory.getInstance();

        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }

        return twitter;
    }

    // TwitterStreamクラスのインスタンスを返す
    // 上と同じ
    public static TwitterStream getTwitterStreamInstance(Context context) {
        String cons_key = context.getString(R.string.twitter_consumer_key);
        String cons_sec = context.getString(R.string.twitter_consumer_secret);

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setDebugEnabled(true)
                .setOAuthConsumerKey(cons_key)
                .setOAuthConsumerSecret(cons_sec)
                .setOAuthAccessToken(null)
                .setOAuthAccessTokenSecret(null);
        TwitterStreamFactory factory = new TwitterStreamFactory(builder.build());
        TwitterStream twitter_stream = factory.getInstance();

        if (hasAccessToken(context)) {
            twitter_stream.setOAuthAccessToken(loadAccessToken(context));
        }

        return twitter_stream;
    }

    // アクセストークンをプレフに保存
    public static void storeAccessToken(Context context, AccessToken access_token) {
        SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        editor.putString(TOKEN, access_token.getToken());
        editor.putString(TOKEN_SECRET, access_token.getTokenSecret());
        editor.commit();
    }

    // アクセストークンをプレフから読み込んで返す
    public static AccessToken loadAccessToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String token = pref.getString(TOKEN, null);
        String token_secret = pref.getString(TOKEN_SECRET, null);
        if (token != null && token_secret != null) {
            return new AccessToken(token, token_secret);
        } else {
            return null;
        }
    }

    // アクセストークンがプレフに存在するか
    public static boolean hasAccessToken(Context context) {
        if (loadAccessToken(context) == null) {
            return false;
        } else {
            return true;
        }
    }


}
