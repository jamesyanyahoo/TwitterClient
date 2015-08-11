package com.yahoo.shopping.twitterclient;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

@Table(name = "tweets")
public class TweetModel extends Model {
	@Column(name = "name")
	private String name;

    @Column(name = "account")
    private String account;

    @Column(name ="profile_image")
	private String profileImageUrl;

    @Column(name = "tweet")
    private String tweet;

    @Column(name = "post_date")
    private Date postDate;

    public TweetModel(String name, String account, String profileImageUrl, String tweet, Date postDate) {
        this.name = name;
        this.account = account;
        this.profileImageUrl = profileImageUrl;
        this.tweet = tweet;
        this.postDate = postDate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

}
