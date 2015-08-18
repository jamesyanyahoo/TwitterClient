package com.yahoo.shopping.twitterclient.models;

import java.util.List;

import twitter4j.Status;
import twitter4j.User;

/**
 * Created by jamesyan on 8/18/15.
 */
public class UserModel {
    private User user;
    private List<Status> tweetList;

    public UserModel(User user, List<Status> tweetList) {
        this.user = user;
        this.tweetList = tweetList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Status> getTweetList() {
        return tweetList;
    }

    public void setTweetList(List<Status> tweetList) {
        this.tweetList = tweetList;
    }
}
