package com.yahoo.shopping.twitterclient.applications;

import com.activeandroid.app.Application;
import com.yahoo.shopping.twitterclient.constants.TwitterConstant;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterClientApplication extends Application {
    private Twitter mTwitter;
    private RequestToken mRequestToken;

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

    public RequestToken getRequestToken() {
        return mRequestToken;
    }

    public void setRequestToken(RequestToken requestToken) {
        mRequestToken = requestToken;
    }
}
