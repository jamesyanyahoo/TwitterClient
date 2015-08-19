package com.yahoo.shopping.twitterclient.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.yahoo.shopping.twitterclient.R;
import com.yahoo.shopping.twitterclient.asynctask.GenericTwitterRequestAsyncTask;
import com.yahoo.shopping.twitterclient.constants.CommandType;
import com.yahoo.shopping.twitterclient.constants.TwitterConstant;
import com.yahoo.shopping.twitterclient.fragments.UserMentionsFragment;
import com.yahoo.shopping.twitterclient.fragments.PostTweetDialogFragment;
import com.yahoo.shopping.twitterclient.fragments.UserTimelineFragment;

import java.util.Arrays;
import java.util.List;

public class TwitterActivity extends AppCompatActivity implements PostTweetDialogFragment.OnFinishEditing {
    private static final String TAG = TwitterActivity.class.getSimpleName();

    UserTimelineFragment mTwitterListFragment;
    UserMentionsFragment mMentionListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_timeline);

        mTwitterListFragment = new UserTimelineFragment();
        mMentionListFragment = new UserMentionsFragment();

        // handle intent get access token
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(TwitterConstant.TWITTER_CALLBACK_URL)) {
            String verifier = uri.getQueryParameter(TwitterConstant.URL_TWITTER_OAUTH_VERIFIER);
            new GenericTwitterRequestAsyncTask(mTwitterListFragment, this).execute(CommandType.GET_ACCESS_TOKEN, verifier);
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TwitterFragmentPageAdapter(getSupportFragmentManager(),
                Arrays.asList(mTwitterListFragment, mMentionListFragment), Arrays.asList("Time Line", "Mentions")));

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabsStrip.setViewPager(viewPager);
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_user_info) {
            Intent intent = new Intent(this, UserInfoActivity.class);
            startActivity(intent);
        }
        return true;
    }

    class TwitterFragmentPageAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList;
        private List<String> mTitleList;

        public TwitterFragmentPageAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> titleList) {
            super(fm);
            mFragmentList = fragmentList;
            mTitleList = titleList;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }
    }

    @Override
    public void onFinishEditing(String tweet) {
        if (tweet == null || tweet.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.empty_tweet_warning), Toast.LENGTH_SHORT).show();
            return;
        }
        postTweet(tweet);
    }

    private void postTweet(String tweet) {
        new GenericTwitterRequestAsyncTask(mTwitterListFragment, this).execute(CommandType.POST_TWEET, tweet);
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        if (mTweetList.size() > 0) {
//            Log.i(TAG, "remove all from cache");
//            new Delete().from(TweetModel.class).execute();
//
//            Log.i(TAG, "store the tweet data in cache");
//            for (TweetModel tweet : mTweetList) {
//                tweet.save();
//            }
//        }
//    }
}
