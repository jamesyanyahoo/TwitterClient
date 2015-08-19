package com.yahoo.shopping.twitterclient.asynctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.yahoo.shopping.twitterclient.applications.TwitterClientApplication;
import com.yahoo.shopping.twitterclient.constants.TwitterConstant;
import com.yahoo.shopping.twitterclient.models.UserModel;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Created by jamesyan on 8/18/15.
 */
public class UserInfoAsyncTask extends AsyncTask<String, Void, UserModel> {
    private static final String TAG = UserInfoAsyncTask.class.getSimpleName();

    private TwitterClientApplication mApplication;
    private Twitter mTwitter;
    private Context mContext;
    private PostGetUserInfo mHandler;

    public UserInfoAsyncTask(PostGetUserInfo handler, Context context) {
        mHandler = handler;
        mContext = context;
        mApplication = (TwitterClientApplication) context.getApplicationContext();
        mTwitter = mApplication.getTwitterClient();
    }

    @Override
    protected UserModel doInBackground(String... params) {
        String screenName = "";

        try {
            if (params.length == 0) {
                SharedPreferences preferences = mContext.getSharedPreferences(TwitterConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
                String accessToken = preferences.getString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_TOKEN, "");
                String accessTokenSecret = preferences.getString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_SECRET, "");

                if (!accessToken.isEmpty() && !accessTokenSecret.isEmpty()) {
                    Log.i(TAG, "get access token: " + accessToken);
                    Log.i(TAG, "get access token secret: " + accessTokenSecret);

                    AccessToken token = new AccessToken(accessToken, accessTokenSecret);

                    User user = mTwitter.showUser(token.getUserId());
                    List<twitter4j.Status> tweets = mTwitter.getMentionsTimeline(new Paging(1, 100));

                    Log.i(TAG, String.valueOf(tweets.size()));

                    return new UserModel(user, tweets);
                }
            } else {
                User user = mTwitter.showUser(params[0]);
                List<twitter4j.Status> tweets = mTwitter.getUserTimeline(user.getScreenName());

                return new UserModel(user, tweets);
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(UserModel user) {
        mHandler.postGetUserInfo(user);
    }

    public interface PostGetUserInfo {
        void postGetUserInfo(UserModel user);
    }
}
