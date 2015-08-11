package com.yahoo.shopping.twitterclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterListActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = TwitterListActivity.class.getSimpleName();

    private static final String PREFERENCE_NAME = "twitter_oauth";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";

    private static RequestToken mRequestToken;
    private String mAccessToken;
    private String mAccessTokenSecret;

    private Twitter mTwitter;
    private List<TweetModel> mTweetList = new ArrayList<TweetModel>();
    private TweetListAdapter mTweetListAdapter;

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

        RecyclerView rvTweetList = (RecyclerView) findViewById(R.id.activity_twitterlist_lv_tweet_list);
        rvTweetList.setLayoutManager(new LinearLayoutManager(this));
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

            new TwitterRequestAsyncTask(this).execute(CommandType.GET_USER_TWEETS);
        } else {
            Log.i(TAG, "should go to login for getting the tokens");
        }
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    public void processLogin() {
        new TwitterRequestAsyncTask(this).execute(CommandType.GET_REQUEST_TOKEN);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.activity_twitterlist_btn_login) {
            processLogin();
        }
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
        }

        private Object doGetUserTweets() {
            Log.i(TAG, "doGetUserTweets");
            try {
                List<twitter4j.Status> list = mTwitter.getHomeTimeline();

                return list;
            } catch (TwitterException e) {
                e.printStackTrace();
            }

            return null;
        }

        private void postGetUserTweets(List<twitter4j.Status> list) {
            Log.i(TAG, "postGetUserTweets");

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
