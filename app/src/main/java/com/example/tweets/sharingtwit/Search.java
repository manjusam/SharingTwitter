package com.example.tweets.sharingtwit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by admin on 17/08/2015.
 */
public class Search extends Activity {
    EditText search_text;
    Button search_button;
    ListView list;
    ProgressDialog progress;
    Context context;
    String searchText;
    ArrayList<String> tweetTexts = new ArrayList();
    /**twitter developer key*/
    public final static String TWIT_KEY = Config.API_KEY;//alter
    /**twitter developer secret*/
    public final static String TWIT_SECRET = Config.API_SECRET;//alter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        search_text= (EditText) findViewById(R.id.searchText);
        search_button= (Button) findViewById(R.id.search_button);
        list= (ListView) findViewById(R.id.search_listView);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new getTweets().execute();
            }
        });

    }
    private class getTweets extends AsyncTask<String, String, ArrayList<String>> {
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            progress.hide();
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(Search.this, android.R.layout.simple_list_item_1, result);
            list.setAdapter(itemsAdapter);
            Toast.makeText(Search.this, "Tweet searched ", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progress = new ProgressDialog(Search.this);
            progress.setMessage("Searching tweet ...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            searchText = search_text.getText().toString();
            progress.show();

        }

        @Override
        protected ArrayList<String> doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            List<twitter4j.Status> tweets = new ArrayList();
            tweetTexts
                    .clear();
            SharedPreferences tweetPrefs = getSharedPreferences("TwitNicePrefs", 0);
            String  userToken = tweetPrefs.getString("ACCESS_TOKEN", null);
            String userSecret = tweetPrefs.getString("ACCESS_TOKEN_SECRET", null);

            //create new Twitter configuration
            Configuration twitConf = new ConfigurationBuilder()
                    .setOAuthConsumerKey(TWIT_KEY)
                    .setOAuthConsumerSecret(TWIT_SECRET)
                    .setOAuthAccessToken(userToken)
                    .setOAuthAccessTokenSecret(userSecret)
                    .build();

            //create Twitter instance for retweeting
            Twitter retweetTwitter = new TwitterFactory(twitConf).getInstance();
            //get tweet data from view tag
           // Twitter mTwitter = getTwitter();
            try {

                tweets = retweetTwitter.search(new Query(searchText)).getTweets();
                for (twitter4j.Status t : tweets) {
                    tweetTexts.add(t.getText() + "\n\n");
                }


              } catch (Exception e) {
                tweetTexts.add("Twitter query failed: " + e.toString());
            }

            return tweetTexts;
        }


    }
}
