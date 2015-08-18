package com.yahoo.shopping.twitterclient.interfaces;

import java.util.List;

import twitter4j.Status;

/**
 * Created by jamesyan on 8/18/15.
 */
public interface TwitterEventListener {
    void postGetRequestToken(Object object);
    void postGetAccessToken();
    void postGetUserTweets(List<Status> list);
    void postPostTweet();
}
