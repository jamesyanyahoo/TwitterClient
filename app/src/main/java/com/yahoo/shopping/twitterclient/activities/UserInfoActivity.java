package com.yahoo.shopping.twitterclient.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yahoo.shopping.twitterclient.R;
import com.yahoo.shopping.twitterclient.adapters.TweetSimpleAdapter;
import com.yahoo.shopping.twitterclient.asynctask.UserInfoAsyncTask;
import com.yahoo.shopping.twitterclient.fragments.UserTimelineFragment;
import com.yahoo.shopping.twitterclient.models.UserModel;

public class UserInfoActivity extends AppCompatActivity implements UserInfoAsyncTask.PostGetUserInfo {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent intent = getIntent();
        if (intent != null) {
            String screenName = intent.getStringExtra(UserTimelineFragment.USER_SCREEN_NAME);
            if (screenName != null && !screenName.isEmpty()) {
                new UserInfoAsyncTask(this, this).execute(screenName);
                return;
            }
        }

        new UserInfoAsyncTask(this, this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void postGetUserInfo(UserModel user) {
        ImageView ivProfile = (ImageView) findViewById(R.id.activity_user_info_iv_profile);
        TextView tvAccount = (TextView) findViewById(R.id.activity_user_info_tv_account);
        TextView tvFollowers = (TextView) findViewById(R.id.activity_user_info_tv_followers);
        TextView tvFollowings = (TextView) findViewById(R.id.activity_user_info_tv_followings);
        ListView lvComments = (ListView) findViewById(R.id.activity_user_info_lv_comments);

        Picasso.with(this).load(user.getUser().getProfileImageURL()).into(ivProfile);
        tvAccount.setText(user.getUser().getScreenName());
        tvFollowers.setText(user.getUser().getFollowersCount() + " followers");
        tvFollowings.setText(user.getUser().getFriendsCount() + " followings");

        lvComments.setAdapter(new TweetSimpleAdapter(this, 0, user.getTweetList()));
    }
}
