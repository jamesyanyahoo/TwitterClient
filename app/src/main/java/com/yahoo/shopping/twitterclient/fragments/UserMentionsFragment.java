package com.yahoo.shopping.twitterclient.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yahoo.shopping.twitterclient.R;
import com.yahoo.shopping.twitterclient.adapters.TweetSimpleAdapter;
import com.yahoo.shopping.twitterclient.applications.TwitterClientApplication;
import com.yahoo.shopping.twitterclient.asynctask.UserInfoAsyncTask;
import com.yahoo.shopping.twitterclient.constants.TwitterConstant;
import com.yahoo.shopping.twitterclient.models.UserModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserMentionsFragment extends Fragment implements UserInfoAsyncTask.PostGetUserInfo {
    private TwitterClientApplication mApplication;
    private Context mContext;
    private View mFragment;

    public UserMentionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();

        // getting access token from preference
        SharedPreferences preferences = getActivity().getSharedPreferences(TwitterConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String accessToken = preferences.getString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_TOKEN, "");
        String accessTokenSecret = preferences.getString(TwitterConstant.PREF_KEY_OAUTH_ACCESS_SECRET, "");

        if (!accessToken.isEmpty() && !accessTokenSecret.isEmpty()) {
            new UserInfoAsyncTask(this, mContext).execute();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragment = inflater.inflate(R.layout.fragment_user_mentions, container, false);
        return mFragment;
    }

    @Override
    public void postGetUserInfo(UserModel user) {
        ListView lvList = (ListView) mFragment.findViewById(R.id.fragment_mentions_lv_list);
        lvList.setAdapter(new TweetSimpleAdapter(mContext, 0, user.getTweetList()));
    }
}
