package com.yahoo.shopping.twitterclient.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.yahoo.shopping.twitterclient.applications.TwitterClientApplication;
import com.yahoo.shopping.twitterclient.constants.CommandType;
import com.yahoo.shopping.twitterclient.constants.TwitterConstant;
import com.yahoo.shopping.twitterclient.interfaces.TwitterEventListener;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by jamesyan on 8/18/15.
 */
public class TwitterRequestAsyncTask extends AsyncTask<Object, Void, Object> {
    private static final String TAG = TwitterRequestAsyncTask.class.getSimpleName();

    private TwitterEventListener mEventListener;
    private TwitterClientApplication mApplication;
    private CommandType mCommandType;
    private Twitter mTwitter;
    private Context mContext;

    public TwitterRequestAsyncTask(TwitterEventListener eventListener, Context context) {
        mEventListener = eventListener;
        mContext = context;
        mApplication = (TwitterClientApplication) context.getApplicationContext();
        mTwitter = mApplication.getTwitterClient();
    }

    private Object doGetRequestToken() {
        Log.i(TAG, "doGetRequestToken");
        try {
            mApplication.setRequestToken(mTwitter.getOAuthRequestToken(TwitterConstant.TWITTER_CALLBACK_URL));
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return mApplication.getRequestToken();
    }

    private Object doGetAccessToken(String verifier) {
        Log.i(TAG, "doGetAccessToken");

        RequestToken requestToken = mApplication.getRequestToken();
        if (requestToken == null) {
            Log.i(TAG, "doGetAccessToken fail: mRequestToken is null");
            return null;
        }

        try {
            AccessToken token = mTwitter.getOAuthAccessToken(requestToken, verifier);
            String accessToken = token.getToken();
            String accessTokenSecret = token.getTokenSecret();

            SharedPreferences preferences = mContext.getSharedPreferences(TwitterConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_TOKEN, accessToken);
            editor.putString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_SECRET, accessTokenSecret);
            editor.apply();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object doGetUserTweets(int pageNumber) {
        Log.i(TAG, "doGetUserTweets");
        try {
            Paging page = new Paging(pageNumber, 20);
            return mTwitter.getHomeTimeline(page);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Object doPostTweet(String tweet) {
        Log.i(TAG, "doPostTweet");

        try {
            mTwitter.updateStatus(tweet);
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return null;
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
                int pageNumber = (int) params[1];
                return doGetUserTweets(pageNumber);
            case POST_TWEET:
                String tweet = (String) params[1];
                return doPostTweet(tweet);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object object) {
        switch (mCommandType) {
            case GET_REQUEST_TOKEN:
                mEventListener.postGetRequestToken(object);
                break;
            case GET_ACCESS_TOKEN:
                mEventListener.postGetAccessToken();
                break;
            case GET_USER_TWEETS:
                mEventListener.postGetUserTweets((List<twitter4j.Status>) object);
                break;
            case POST_TWEET:
                mEventListener.postPostTweet();
                break;
        }
    }
}
