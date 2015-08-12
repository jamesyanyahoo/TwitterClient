package com.yahoo.shopping.twitterclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterListActivity extends AppCompatActivity
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = TwitterListActivity.class.getSimpleName();

    private static final String PREFERENCE_NAME = "twitter_oauth";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

    private static RequestToken mRequestToken;
    private String mAccessToken;
    private String mAccessTokenSecret;

    private int currentPage = 1;

    private Twitter mTwitter;
    private List<TweetModel> mTweetList = new ArrayList<TweetModel>();
    private TweetListAdapter mTweetListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitterlist);

        mTwitter = ((TwitterClientApplication) getApplication()).getTwitterClient();

        // handle intent get access token
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(TwitterConstant.TWITTER_CALLBACK_URL)) {
            String verifier = uri.getQueryParameter(TwitterConstant.URL_TWITTER_OAUTH_VERIFIER);
            new TwitterRequestAsyncTask(this).execute(CommandType.GET_ACCESS_TOKEN, verifier);
        }

        // setup callbacks
        Button btnLogin = (Button) findViewById(R.id.activity_twitterlist_btn_login);
        btnLogin.setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_twitterlist_ly_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView rvTweetList = (RecyclerView) findViewById(R.id.activity_twitterlist_lv_tweet_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweetList.setLayoutManager(linearLayoutManager);
        rvTweetList.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager, mSwipeRefreshLayout) {
            @Override
            public void onLoadMore(int current_page) {
                TwitterListActivity.this.loadMoreTweets();
            }
        });
        mTweetListAdapter = new TweetListAdapter(this, mTweetList);
        rvTweetList.setAdapter(mTweetListAdapter);

        FloatingActionButton btnPostTweet = (FloatingActionButton) findViewById(R.id.activity_twitterlist_btn_post_tweet);
        btnPostTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                PostTweetDialogFragment dialog = PostTweetDialogFragment.newInstance("Settings");
                dialog.show(fragmentManager, "fragment_dialog_config");
            }
        });

        // getting access token from preference
        SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        mAccessToken = preferences.getString(PREF_KEY_OAUTH_TOKEN, "");
        mAccessTokenSecret = preferences.getString(PREF_KEY_OAUTH_SECRET, "");

        if (!mAccessToken.isEmpty() && !mAccessTokenSecret.isEmpty()) {
            Log.i(TAG, "get access token: " + mAccessToken);
            Log.i(TAG, "get access token secret: " + mAccessTokenSecret);

            AccessToken accessToken = new AccessToken(mAccessToken, mAccessTokenSecret);
            mTwitter.setOAuthAccessToken(accessToken);

            if (isConnectingToInternet()) {
                refreshTweetList();
            } else {
                loadDataFromCache();
            }
        } else {
            Log.i(TAG, "should go to login for getting the tokens");
            showInformationPanel(true);
            showLoginButton();
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mTweetList.size()>0) {
            Log.i(TAG, "remove all from cache");
            new Delete().from(TweetModel.class).execute();

            Log.i(TAG, "store the tweet data in cache");
            for (TweetModel tweet : mTweetList) {
                tweet.save();
            }
        }
    }

    private void processLogin() {
        new TwitterRequestAsyncTask(this).execute(CommandType.GET_REQUEST_TOKEN);
    }

    private void refreshTweetList() {
        new TwitterRequestAsyncTask(this).execute(CommandType.GET_USER_TWEETS);
    }

    private void loadMoreTweets() {
        currentPage++;
        new TwitterRequestAsyncTask(this).execute(CommandType.GET_USER_TWEETS);

        Log.i(TAG, "fetch page: " + currentPage);
    }

    private void loadDataFromCache() {
        Log.i(TAG, "loadDataFromCache");

        List<TweetModel> list = new Select().from(TweetModel.class).execute();
        mTweetList.clear();
        mTweetList.addAll(list);
        mTweetListAdapter.notifyDataSetChanged();
    }

    private boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private void showInformationPanel(boolean isShow) {
        if (isShow) {
            findViewById(R.id.activity_twitterlist_fl_info).setVisibility(View.VISIBLE);
            findViewById(R.id.activity_twitterlist_lv_tweet_list).setVisibility(View.INVISIBLE);
            findViewById(R.id.activity_twitterlist_btn_post_tweet).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.activity_twitterlist_fl_info).setVisibility(View.INVISIBLE);
            findViewById(R.id.activity_twitterlist_lv_tweet_list).setVisibility(View.VISIBLE);
            findViewById(R.id.activity_twitterlist_btn_post_tweet).setVisibility(View.VISIBLE);
        }
    }

    private void showLoginButton() {
            findViewById(R.id.activity_twitterlist_tv_info).setVisibility(View.INVISIBLE);
            findViewById(R.id.activity_twitterlist_btn_login).setVisibility(View.VISIBLE);
    }

    private void showInfo(String text) {
        findViewById(R.id.activity_twitterlist_tv_info).setVisibility(View.VISIBLE);
        findViewById(R.id.activity_twitterlist_btn_login).setVisibility(View.INVISIBLE);

        TextView tvInfo = (TextView) findViewById(R.id.activity_twitterlist_tv_info);
        tvInfo.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.activity_twitterlist_btn_login) {
            processLogin();
        }
    }

    @Override
    public void onRefresh() {
        refreshTweetList();
    }

    enum CommandType {
        GET_REQUEST_TOKEN, GET_ACCESS_TOKEN, GET_USER_TWEETS
    }

    private class TwitterRequestAsyncTask extends AsyncTask<Object, Void, Object> {
        private Context mContext;
        private CommandType mCommandType;

        public TwitterRequestAsyncTask(Context context) {
            mContext = context;
        }

        private Object doGetRequestToken() {
            Log.i(TAG, "doGetRequestToken");
            try {
                mRequestToken = mTwitter.getOAuthRequestToken(TwitterConstant.TWITTER_CALLBACK_URL);
            } catch (TwitterException e) {
                mRequestToken = null;
                e.printStackTrace();
            }

            return mRequestToken;
        }

        private void postGetRequestToken(Object object) {
            Log.i(TAG, "postGetRequestToken");
            RequestToken token = (RequestToken) object;

            if (token != null) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(token.getAuthorizationURL())));
            }
        }

        private Object doGetAccessToken(String verifier) {
            Log.i(TAG, "doGetAccessToken");

            if (mRequestToken == null) {
                Log.i(TAG, "doGetAccessToken fail: mRequestToken  is null");
                return null;
            }

            try {
                AccessToken token = mTwitter.getOAuthAccessToken(mRequestToken, verifier);
                mAccessToken = token.getToken();
                mAccessTokenSecret = token.getTokenSecret();

                SharedPreferences preferences = mContext.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_KEY_OAUTH_TOKEN, mAccessToken);
                editor.putString(PREF_KEY_OAUTH_SECRET, mAccessTokenSecret);
                editor.apply();
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void postGetAccessToken() {
            refreshTweetList();
        }

        private Object doGetUserTweets() {
            Log.i(TAG, "doGetUserTweets");
            try {
                Paging page = new Paging(currentPage, 20);

                List<twitter4j.Status> list = mTwitter.getHomeTimeline(page);

                return list;
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void postGetUserTweets(List<twitter4j.Status> list) {
            Log.i(TAG, "postGetUserTweets");

            if (list == null)
                return;

            if (list.size() == 0) {
                if (currentPage != 1) {
                    currentPage--;
                }
            }

            if (currentPage == 1) {
                mTweetList.clear();
                mTweetListAdapter.notifyDataSetChanged();
            }

            for (twitter4j.Status status : list) {
                TweetModel tweet = new TweetModel(
                        status.getUser().getName(),
                        status.getUser().getScreenName(),
                        status.getUser().getMiniProfileImageURL(),
                        status.getText(),
                        status.getCreatedAt());

                mTweetList.add(tweet);
                mTweetListAdapter.notifyItemInserted(mTweetList.size());
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected Object doInBackground(Object... params) {
            mCommandType = (CommandType) params[0];

            switch (mCommandType) {
                case GET_REQUEST_TOKEN:
                    return doGetRequestToken();
                case GET_ACCESS_TOKEN:
                    String verifer = (String) params[1];
                    return doGetAccessToken(verifer);
                case GET_USER_TWEETS:
                    return doGetUserTweets();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object object) {
            switch (mCommandType) {
                case GET_REQUEST_TOKEN:
                    postGetRequestToken(object);
                    break;
                case GET_ACCESS_TOKEN:
                    postGetAccessToken();
                    break;
                case GET_USER_TWEETS:
                    postGetUserTweets((List<twitter4j.Status>) object);
                    break;
            }
        }
    }

}
