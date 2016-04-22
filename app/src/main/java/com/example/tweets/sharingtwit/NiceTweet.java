package com.example.tweets.sharingtwit;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
/**
 * Created by admin on 30/07/2015.
 */
public class NiceTweet extends Activity implements OnClickListener {
    /**
     * shared preferences for user twitter details
     */

    private SharedPreferences tweetPrefs;
    /**
     * twitter object*
     */
    private Twitter tweetTwitter;

    /**
     * twitter key
     */
    public final static String TWIT_KEY = Config.API_KEY;
    /**
     * twitter secret
     */
    public final static String TWIT_SECRET = Config.API_SECRET;

    /**
     * the update ID for this tweet if it is a reply
     */
    private long tweetID = 0;
    /**
     * the username for the tweet if it is a reply
     */
    private String tweetName = "";
    EditText tweet_TXT;
    /*
 * onCreate called when activity is created
 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set tweet layout
        setContentView(R.layout.tweet);
    }

    /*
 * Call setup method when this activity starts
 */
    @Override
    public void onResume() {
        super.onResume();
        //call helper method
        setupTweet();
    }

    /**
     * Method called whenever this Activity starts
     * - get ready to tweet
     * Sets up twitter and onClick listeners
     * - also sets up for replies
     */
    private void setupTweet() {
        //prepare to tweet
        //get preferences for user twitter details
        tweetPrefs = getSharedPreferences("TwitNicePrefs", 0);

        //get user token and secret for authentication
        String userToken = tweetPrefs.getString("ACCESS_TOKEN", null);
        String userSecret = tweetPrefs.getString("ACCESS_TOKEN_SECRET", null);

        //create a new twitter configuration usign user details
        Configuration twitConf = new ConfigurationBuilder()
                .setOAuthConsumerKey(TWIT_KEY)
                .setOAuthConsumerSecret(TWIT_SECRET)
                .setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret)
                .build();

        //create a twitter instance
        tweetTwitter = new TwitterFactory(twitConf).getInstance();
        //get any data passed to this intent for a reply
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //get the ID of the tweet we are replying to
            tweetID = extras.getLong("tweetID");
            //get the user screen name for the tweet we are replying to
            tweetName = extras.getString("tweetUser");


            //get a reference to the text field for tweeting
           tweet_TXT = (EditText) findViewById(R.id.tweettext);
            //start the tweet text for the reply @username
           tweet_TXT.setText("@" + tweetName + " ");
            //set the cursor to the end of the text for entry
            tweet_TXT.setSelection(tweet_TXT.getText().length());
        } else {
           tweet_TXT= (EditText) findViewById(R.id.tweettext);
            tweet_TXT.setText("");
        }
        //set up listener for choosing home button to go to timeline
        LinearLayout tweetClicker = (LinearLayout) findViewById(R.id.homebtn);
        tweetClicker.setOnClickListener(this);

        //set up listener for send tweet button
        Button tweetButton = (Button) findViewById(R.id.dotweet);
        tweetButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //tweet_TXT=(EditText) findViewById(R.id.tweettext);
        //find out which view has been clicked
        switch (v.getId()) {
            case R.id.dotweet:
                //send tweet
                  //new DoingTweetReply((EditText)v.findViewById(R.id.tweettext)).execute();
                new DoingTweetReply().execute("my string");

                break;

            case R.id.homebtn:
                //go to the home timeline
               tweet_TXT.setText("");
                break;
            default:
                break;
        }
        finish();
    }

    private class DoingTweetReply extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tweet_TXT= (EditText) findViewById(R.id.tweettext);
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String toTweet = tweet_TXT.getText().toString();
            try {
                //handle replies
                if (tweetName.length() > 0)
                    tweetTwitter.updateStatus(new StatusUpdate(toTweet).inReplyToStatusId(tweetID));

                    //handle normal tweets
                else
                    tweetTwitter.updateStatus(toTweet);

                //reset the edit text

            } catch (TwitterException te) {
                Log.e("NiceTweet", te.getMessage());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            tweet_TXT.setText("");// this is UI updation so never use like this line in do in background thread.
            Toast.makeText(getApplicationContext(),"Tweeted",Toast.LENGTH_LONG).show();
        }
    }





}
