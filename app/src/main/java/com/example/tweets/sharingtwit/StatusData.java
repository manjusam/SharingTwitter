package com.example.tweets.sharingtwit;

/**
 * Created by admin on 31/07/2015.
 */
public class StatusData {
    /**tweet ID*/
    private long tweetID;

    /**user screen name of tweeter*/
    private String tweetUser;

    public StatusData(long ID, String screenName) {
        //instantiate variables
        tweetID=ID;
        tweetUser=screenName;
    }
    /**
     * Get the ID of the tweet
     * @return tweetID as a long
     */
    public long getID() {return tweetID;}
    /**
     * Get the user screen name for the tweet
     * @return tweetUser as a String
     */
    public String getUser() {return tweetUser;}
}
