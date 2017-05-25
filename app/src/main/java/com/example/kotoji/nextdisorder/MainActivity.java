package com.example.kotoji.nextdisorder;

/**
 * Created by kotoji on 13/9/2012.
 */

import twitter4j.ResponseList;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.StatusListener;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.image.SmartImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends Activity {

    private static final String SELECTED_TWEET_ID = "reply_status_navi3wr9uqbntu0";

    private TweetAdapter _adapter;
    private Twitter _twitter;
    private TwitterStream _tw_stream;
    private ListView _timeline;
    private long selected_tweet_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // アクションバーのカスタマイズ
        ActionBar actionbar = getActionBar();
        // タイトルとアイコンを非表示にする。
        actionbar.setDisplayShowTitleEnabled(false);
        actionbar.setDisplayUseLogoEnabled(false); // 端末によって効いたり効かなかったり

        // アクセストークンの取得がまだならOAuthActivityへ行って取ってきてね
        if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(this, OAuthActivity.class);
            startActivity(intent);
            finish();
        } else { // Timeline を表示
            _timeline = (ListView) findViewById(R.id.timeline);
            _adapter = new TweetAdapter(this);
            // リストビューにアダプターを設定
            _timeline.setAdapter(_adapter);
            // コンテキストメニュー登録
            registerForContextMenu(_timeline);

            _twitter = TwitterUtils.getTwitterInstance(this);
            updateTimeLine();

            // UserStreamを設定する
            _tw_stream = TwitterUtils.getTwitterStreamInstance(this);
            StatusListener listener = new StatusListener() {
                @Override
                public void onStatus(twitter4j.Status status) {
                    _adapter.insert(status, 0);
                    //_timeline.setSelection(0);

                    // debug
                    Log.i("UserName: "+status.getUser().getName(),"Tweet: "+status.getText());
                }
                @Override
                public void onDeletionNotice(twitter4j.StatusDeletionNotice statusDeletionNotice) {}
                @Override
                public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
                @Override
                public void onScrubGeo(long userId, long upToStatusId) {}
                @Override
                public void onStallWarning(twitter4j.StallWarning warning) {}
                @Override
                public void onException(Exception ex) {}
            };
            _tw_stream.addListener(listener);
            _tw_stream.user();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // ListViewのアイテム長押しでコンテクストメニューを表示するのでその処理
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
        super.onCreateContextMenu(menu, view, info);
        AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) info;
        twitter4j.Status _tweet = (twitter4j.Status) _timeline.getItemAtPosition(adapterInfo.position);
        twitter4j.Status tweet = null;
        if (_tweet.isRetweet()) {
            tweet = _tweet.getRetweetedStatus();
        } else {
            tweet = _tweet;
        }

        selected_tweet_id = tweet.getId();

        menu.setHeaderTitle("@" + tweet.getUser().getScreenName());
        menu.add(0, R.id.REPLY_BUTTON_ID, 0, "replay");
        menu.add(0, R.id.FAVORITE_BUTTON_ID, 0, "favorite");
        menu.add(0, R.id.RETWEET_BUTTON_ID, 0, "retweet");
    }

    // 上のコンテクストメニューの挙動
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.REPLY_BUTTON_ID:
                Intent intent = new Intent(this, ReplyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong(SELECTED_TWEET_ID, selected_tweet_id);
                intent.putExtras(bundle);
                startActivity(intent);
                return true;
            case R.id.FAVORITE_BUTTON_ID:
                AsyncTask<Long, Void, Boolean> fav_task = new AsyncTask<Long, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Long... args) {
                        try {
                            _twitter.createFavorite(args[0]);
                            return true;
                        } catch (TwitterException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result) {
                            showToast("Favorited.");
                        } else {
                            showToast("Failed.");
                        }
                    }
                };
                fav_task.execute(selected_tweet_id);
                return true;
            case R.id.RETWEET_BUTTON_ID:
                AsyncTask<Long, Void, Boolean> ret_task = new AsyncTask<Long, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Long... args) {
                        try {
                            _twitter.retweetStatus(args[0]);
                            return true;
                        } catch (TwitterException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        if (result) {
                            showToast("Retweeted.");
                        } else {
                            showToast("Failed.");
                        }
                    }
                };
                ret_task.execute(selected_tweet_id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    // Android標準のメニューボタンのアイテムが押されたとき
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_button:
                updateTimeLine();
                return true;
            case R.id.tweet_button:
                Intent intent = new Intent(this, TweetActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private  void updateTimeLine() {
        AsyncTask<Void, Void, List<twitter4j.Status>> task = new AsyncTask<Void, Void, List<twitter4j.Status>>() {
            @Override
            protected List<twitter4j.Status> doInBackground(Void... voids) {
                try {
                    return _twitter.getHomeTimeline();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(List<twitter4j.Status> tweets) {
                if (tweets != null) {
                    _adapter.clear();
                    for (twitter4j.Status tweet : tweets) {
                        _adapter.add(tweet);
                    }
                    _timeline.setSelection(0);
                } else {
                    showToast("Update Failed.");
                }
            }
        };
        task.execute();
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private  class TweetAdapter extends ArrayAdapter<twitter4j.Status> {

        private LayoutInflater _inflater;

        public TweetAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_1);
            _inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int pos, View convView, ViewGroup parent) {
            if (convView == null) {
                convView = _inflater.inflate(R.layout.item_tweet, null);
            }
            twitter4j.Status _item = getItem(pos);
            twitter4j.Status item = null;
            if (_item.isRetweet()) {
                item  = _item.getRetweetedStatus();
                TextView retweeted_by = (TextView) convView.findViewById(R.id.retweeted_by);
                retweeted_by.setVisibility(TextView.VISIBLE);
                retweeted_by.setText("Reteeted by " + _item.getUser().getName());
                SmartImageView icon_rt = (SmartImageView) convView.findViewById(R.id.icon_retweeted_by);
                icon_rt.setVisibility(SmartImageView.VISIBLE);
                icon_rt.setImageUrl(_item.getUser().getProfileImageURL());
            } else {
                item = _item;
                TextView retweeted_by = (TextView) convView.findViewById(R.id.retweeted_by);
                retweeted_by.setVisibility(TextView.GONE);
                SmartImageView icon_rt = (SmartImageView) convView.findViewById(R.id.icon_retweeted_by);
                icon_rt.setVisibility(SmartImageView.GONE);

            }
            TextView screen_name = (TextView) convView.findViewById(R.id.screen_name);
            screen_name.setText(item.getUser().getName());
            TextView twitter_id = (TextView) convView.findViewById(R.id.twitter_id);
            twitter_id.setText("@" + item.getUser().getScreenName());
            TextView tweet_date = (TextView) convView.findViewById(R.id.tweet_date);
            Date date = item.getCreatedAt();
            long delta_sec = (new Date().getTime() - date.getTime()) / 1000;
            int delta_time = 0;
            String time_unit = "secs";
            if (delta_sec >= 60*60*12) {
                time_unit = "jrs";
                delta_time = (int) (delta_sec / (60*60*12));
            } else if (delta_sec >= 60*60) {
                time_unit = "hrs";
                delta_time = (int) (delta_sec / (60*60));
            } else if (delta_sec >= 60){
                time_unit = "mins";
                delta_time = (int) delta_sec / 60;
            } else {
                delta_time = (int) delta_sec;
            }
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            tweet_date.setText(new Integer(delta_time).toString() + time_unit);
            TextView text = (TextView) convView.findViewById(R.id.text);
            text.setText(item.getText());
            SmartImageView icon = (SmartImageView) convView.findViewById(R.id.icon);
            icon.setImageUrl(item.getUser().getProfileImageURL());
            TextView via = (TextView) convView.findViewById(R.id.tweet_via);
            via.setText(item.getSource().replaceAll("<.+?>", ""));
            return convView;
        }
    }
}


