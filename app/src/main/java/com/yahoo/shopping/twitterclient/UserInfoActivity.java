package com.yahoo.shopping.twitterclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yahoo.shopping.twitterclient.adapters.UserTweetListAdapter;
import com.yahoo.shopping.twitterclient.asynctask.TwitterRequestUserInfoAsyncTask;
import com.yahoo.shopping.twitterclient.fragments.TwitterListFragment;
import com.yahoo.shopping.twitterclient.models.UserModel;

public class UserInfoActivity extends AppCompatActivity implements TwitterRequestUserInfoAsyncTask.PostGetUserInfo {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent intent = getIntent();
        if (intent != null) {
            String screenName = intent.getStringExtra(TwitterListFragment.USER_SCREEN_NAME);
            if (screenName != null && !screenName.isEmpty()) {
                new TwitterRequestUserInfoAsyncTask(this, this).execute(screenName);
                return;
            }
        }

        new TwitterRequestUserInfoAsyncTask(this, this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

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

        lvComments.setAdapter(new UserTweetListAdapter(this, 0, user.getTweetList()));
    }
}
