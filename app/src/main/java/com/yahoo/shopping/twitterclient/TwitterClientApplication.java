package com.yahoo.shopping.twitterclient;



import com.activeandroid.app.Application;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClientApplication extends Application {
    private Twitter mTwitter;

    public TwitterClientApplication() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(TwitterConstant.TWITTER_CONSUMER_KEY);
        builder.setOAuthConsumerSecret(TwitterConstant.TWITTER_CONSUMER_SECRET);
        Configuration configuration = builder.build();

        TwitterFactory factory = new TwitterFactory(configuration);
        mTwitter = factory.getInstance();
    }

    public Twitter getTwitterClient() {
        return mTwitter;
    }
}
