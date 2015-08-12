package com.yahoo.shopping.twitterclient.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yahoo.shopping.twitterclient.R;
import com.yahoo.shopping.twitterclient.models.TweetModel;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.List;

/**
 * Created by jamesyan on 8/11/15.
 */
public class TweetListAdapter extends RecyclerView.Adapter<TweetListAdapter.ViewHolder> {
    private Context mContext;
    private List<TweetModel> mTweetList;

    public TweetListAdapter(Context context, List<TweetModel> objects) {
        this.mContext = context;
        this.mTweetList = objects;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.listitem_twitterlist_item, null);

        TextView tvName = (TextView) view.findViewById(R.id.listitem_twitterlist_item_tv_name);
        TextView tvAccount = (TextView) view.findViewById(R.id.listitem_twitterlist_item_tv_account);
        ImageView ivProfile = (ImageView) view.findViewById(R.id.listitem_twitterlist_item_iv_profile_image);
        TextView tvTweet = (TextView) view.findViewById(R.id.listitem_twitterlist_item_tv_tweet);
        TextView tvPostDate = (TextView) view.findViewById(R.id.listitem_twitterlist_item_tv_post_date);

        return new ViewHolder(view, tvName, tvAccount, ivProfile, tvTweet, tvPostDate);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        TweetModel tweet = mTweetList.get(i);

        viewHolder.mName.setText(tweet.getName());
        viewHolder.mAccount.setText("@" + tweet.getAccount());
        viewHolder.mTweet.setText(Html.fromHtml(tweet.getTweet()));
        viewHolder.mPostDate.setText(new PrettyTime().format(tweet.getPostDate()));

        Picasso.with(mContext).load(tweet.getProfileImageUrl()).into(viewHolder.mProfileImage);
    }

    @Override
    public int getItemCount() {
        return mTweetList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mName;
        public TextView mAccount;
        public ImageView mProfileImage;
        public TextView mTweet;
        public TextView mPostDate;

        public ViewHolder(View itemView, TextView mName, TextView mAccount, ImageView mProfileImage, TextView mTweet, TextView mPostDate) {
            super(itemView);

            this.mName = mName;
            this.mAccount = mAccount;
            this.mProfileImage = mProfileImage;
            this.mTweet = mTweet;
            this.mPostDate = mPostDate;
        }
    }
}
