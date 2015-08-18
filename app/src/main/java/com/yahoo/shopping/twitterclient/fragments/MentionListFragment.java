package com.yahoo.shopping.twitterclient.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yahoo.shopping.twitterclient.R;
import com.yahoo.shopping.twitterclient.adapters.UserTweetListAdapter;
import com.yahoo.shopping.twitterclient.applications.TwitterClientApplication;
import com.yahoo.shopping.twitterclient.asynctask.TwitterRequestUserInfoAsyncTask;
import com.yahoo.shopping.twitterclient.models.UserModel;

import twitter4j.Twitter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MentionListFragment extends Fragment implements TwitterRequestUserInfoAsyncTask.PostGetUserInfo {
    private TwitterClientApplication mApplication;
    private Context mContext;
    private View mFragment;

    public MentionListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        new TwitterRequestUserInfoAsyncTask(this, mContext).execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragment = inflater.inflate(R.layout.fragment_mention_list, container, false);
        return mFragment;
    }

    @Override
    public void postGetUserInfo(UserModel user) {
        ListView lvList = (ListView) mFragment.findViewById(R.id.fragment_mentions_lv_list);
        lvList.setAdapter(new UserTweetListAdapter(mContext, 0, user.getTweetList()));
    }
}
