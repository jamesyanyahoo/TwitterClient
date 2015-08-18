package com.yahoo.shopping.twitterclient.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.yahoo.shopping.twitterclient.R;
import com.yahoo.shopping.twitterclient.adapters.TweetListAdapter;
import com.yahoo.shopping.twitterclient.applications.TwitterClientApplication;
import com.yahoo.shopping.twitterclient.asynctask.TwitterRequestAsyncTask;
import com.yahoo.shopping.twitterclient.constants.CommandType;
import com.yahoo.shopping.twitterclient.constants.TwitterConstant;
import com.yahoo.shopping.twitterclient.interfaces.EndlessRecyclerOnScrollListener;
import com.yahoo.shopping.twitterclient.interfaces.TwitterEventListener;
import com.yahoo.shopping.twitterclient.models.TweetModel;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Created by jamesyan on 8/18/15.
 */
public class TwitterListFragment extends Fragment
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, TwitterEventListener {
    private static final String TAG = TwitterListFragment.class.getSimpleName();

    public static final String USER_SCREEN_NAME = "USER_SCREEN_NAME";

    private int currentPage = 1;

    private Twitter mTwitter;
    private List<TweetModel> mTweetList = new ArrayList<>();
    private TweetListAdapter mTweetListAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private String mAccessToken;
    private String mAccessTokenSecret;

    private View mFragmentView;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        mTwitter = ((TwitterClientApplication) getActivity().getApplication()).getTwitterClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragmentView = inflater.inflate(R.layout.fragment_twitterlist, null);

        // setup callbacks
        Button btnLogin = (Button) mFragmentView.findViewById(R.id.activity_twitterlist_btn_login);
        btnLogin.setOnClickListener(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) mFragmentView.findViewById(R.id.activity_twitterlist_ly_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView rvTweetList = (RecyclerView) mFragmentView.findViewById(R.id.activity_twitterlist_lv_tweet_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        rvTweetList.setLayoutManager(linearLayoutManager);
        rvTweetList.setOnScrollListener(new EndlessRecyclerOnScrollListener(linearLayoutManager, mSwipeRefreshLayout) {
            @Override
            public void onLoadMore(int current_page) {
                TwitterListFragment.this.loadMoreTweets();
            }
        });
        mTweetListAdapter = new TweetListAdapter(getActivity(), mTweetList);
        rvTweetList.setAdapter(mTweetListAdapter);

        FloatingActionButton btnPostTweet = (FloatingActionButton) mFragmentView.findViewById(R.id.activity_twitterlist_btn_post_tweet);
        btnPostTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                PostTweetDialogFragment dialog = PostTweetDialogFragment.newInstance();
                dialog.show(fragmentManager, "fragment_dialog_config");
            }
        });

        // getting access token from preference
        SharedPreferences preferences = getActivity().getSharedPreferences(TwitterConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mAccessToken = preferences.getString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_TOKEN, "");
        mAccessTokenSecret = preferences.getString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_SECRET, "");

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
            Log.i(TAG, "should go to menu for getting the tokens");
            showInformationPanel(true);
            showLoginButton();
        }

        return mFragmentView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.activity_twitterlist_btn_login) {
            processLogin();
        }
    }

    private void processLogin() {
        new TwitterRequestAsyncTask(this, mContext).execute(CommandType.GET_REQUEST_TOKEN);
    }

    private void refreshTweetList() {
        new TwitterRequestAsyncTask(this, mContext).execute(CommandType.GET_USER_TWEETS, currentPage);
    }

    private void loadMoreTweets() {
        new TwitterRequestAsyncTask(this, mContext).execute(CommandType.GET_USER_TWEETS, ++currentPage);

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
        ConnectivityManager connectivity = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] networkInfo = connectivity.getAllNetworkInfo();

            if (networkInfo != null) {
                for (NetworkInfo info: networkInfo) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    private void showInformationPanel(boolean isShow) {
        if (isShow) {
            mFragmentView.findViewById(R.id.activity_twitterlist_fl_info).setVisibility(View.VISIBLE);
            mFragmentView.findViewById(R.id.activity_twitterlist_lv_tweet_list).setVisibility(View.INVISIBLE);
            mFragmentView.findViewById(R.id.activity_twitterlist_btn_post_tweet).setVisibility(View.INVISIBLE);
        } else {
            mFragmentView.findViewById(R.id.activity_twitterlist_fl_info).setVisibility(View.INVISIBLE);
            mFragmentView.findViewById(R.id.activity_twitterlist_lv_tweet_list).setVisibility(View.VISIBLE);
            mFragmentView.findViewById(R.id.activity_twitterlist_btn_post_tweet).setVisibility(View.VISIBLE);
        }
    }

    private void showLoginButton() {
        mFragmentView.findViewById(R.id.activity_twitterlist_tv_info).setVisibility(View.INVISIBLE);
        mFragmentView.findViewById(R.id.activity_twitterlist_btn_login).setVisibility(View.VISIBLE);
    }

    private void showInfo(String text) {
        mFragmentView.findViewById(R.id.activity_twitterlist_tv_info).setVisibility(View.VISIBLE);
        mFragmentView.findViewById(R.id.activity_twitterlist_btn_login).setVisibility(View.INVISIBLE);

        TextView tvInfo = (TextView) mFragmentView.findViewById(R.id.activity_twitterlist_tv_info);
        tvInfo.setText(text);
    }


    @Override
    public void onRefresh() {
        refreshTweetList();
    }


    @Override
    public void postGetRequestToken(Object object) {
        Log.i(TAG, "postGetRequestToken");
        RequestToken token = (RequestToken) object;

        if (token != null) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(token.getAuthorizationURL())));
        }
    }

    @Override
    public void postGetAccessToken() {
        showInfo(mContext.getResources().getString(R.string.loading));
        refreshTweetList();
    }

    @Override
    public void postGetUserTweets(List<Status> list) {
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
        showInformationPanel(false);
    }

    @Override
    public void postPostTweet() {
        Log.i(TwitterListFragment.TAG, "postPostTweet");

        refreshTweetList();
    }
}
