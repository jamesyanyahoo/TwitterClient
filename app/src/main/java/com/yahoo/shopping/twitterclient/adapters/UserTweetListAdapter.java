package com.yahoo.shopping.twitterclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yahoo.shopping.twitterclient.R;

import java.util.List;

import twitter4j.Status;

/**
 * Created by jamesyan on 8/19/15.
 */
public class UserTweetListAdapter extends ArrayAdapter<Status> {
    private Context mContext;
    private List<Status> mTweetList;

    public UserTweetListAdapter(Context context, int resource, List<Status> objects) {
        super(context, resource, objects);

        mContext = context;
        mTweetList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_user_tweet_list, null);
        }

        Status status = mTweetList.get(position);
        ImageView ivProfile = (ImageView) convertView.findViewById(R.id.listitem_user_tweetlist_iv_profile);
        TextView tvTweet = (TextView) convertView.findViewById(R.id.listitem_user_tweetlist_iv_tweet);

        Picasso.with(mContext).load(status.getUser().getProfileImageURL()).into(ivProfile);
        tvTweet.setText(status.getText());

        return convertView;
    }
}
