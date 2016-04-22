package com.example.tweets.sharingtwit;
import android.annotation.TargetApi;
import android.content.Context;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Build;

import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.Toast;
import android.net.Uri;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.util.TimeSpanConverter;

import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.util.Log;
import java.io.InputStream;
import java.net.URL;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.text.format.DateUtils;
import android.widget.TextView;
/**
 * Created by admin on 21/07/2015.
 */
public class UpdateAdapter extends SimpleCursorAdapter {
     Bitmap bitmap;
    /**twitter developer key*/
    public final static String TWIT_KEY = Config.API_KEY;//alter
    /**twitter developer secret*/
    public final static String TWIT_SECRET = Config.API_SECRET;//alter

    /**strings representing database column names to map to views*/
    static final String[] from = { "update_text", "user_screen",
            "update_time", "user_img" };
    /**view item IDs for mapping database record values to*/
    static final int[] to = { R.id.updateText, R.id.userScreen,
            R.id.updateTime, R.id.userImg };
    public String LOG_TAG = "UPDATE_ADAPTER";

    Cursor cursor;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UpdateAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.update, c, from, to, flags);
        //calling the  simple cursor adapter constructor

    }


    /*
     * Bind the data to the visible views
     */
    @Override
    public void bindView(View row, Context context, Cursor cursor) {
        super.bindView(row, context, cursor);
        try {



            //get profile image
            String profileURL =
                  cursor.getString(cursor.getColumnIndex("user_img"));
             new LoadPhoto((ImageView)row.findViewById(R.id.userImg)).execute(profileURL);

        }
        catch(Exception te)
        {
            Log.e(LOG_TAG, "catching the error"+ te.getMessage());
        }
        //get the update time
        long createdAt = cursor.getLong(cursor.getColumnIndex("update_time"));
        //get the update time view
        TextView textCreatedAt = (TextView)row.findViewById(R.id.updateTime);
        TimeSpanConverter converter = new TimeSpanConverter();
        //adjust the way the time is displayed to make it human-readable
        textCreatedAt.setText(converter.toTimeSpanString(createdAt)+" ");
        //get the status ID
        long statusID = cursor.getLong(cursor.getColumnIndex("twitter_id"));
        //get the user name
        String statusName = cursor.getString(cursor.getColumnIndex("user_screen"));
        //create a StatusData object to store these
        StatusData tweetData = new StatusData(statusID, statusName);
        //set the status data object as tag for both retweet and reply buttons in this view
        row.findViewById(R.id.retweet).setTag(tweetData);
        row.findViewById(R.id.reply).setTag(tweetData);

        //setup onclick listeners for the retweet and reply buttons
        row.findViewById(R.id.retweet).setOnClickListener(tweetListener);
        row.findViewById(R.id.reply).setOnClickListener(tweetListener);
        //setup  onclick for the user screen name within the tweet
        row.findViewById(R.id.userScreen).setOnClickListener(tweetListener);



        }
    /**
     * tweetListener handles clicks of reply and retweet buttons
     * - also handles clicking the user name within a tweet
     */
    private OnClickListener tweetListener = new OnClickListener() {
        //onClick method
        public void onClick(View v) {
//which view was clicked
            switch(v.getId()) {
                //reply button pressed
                case R.id.reply:
                    //implement reply
                    //create an intent for sending a new tweet
                    Intent replyIntent = new Intent(v.getContext(), NiceTweet.class);
                    //get the data from the tag within the button view
                    StatusData theData = (StatusData)v.getTag();
                    //pass the status ID
                    replyIntent.putExtra("tweetID", theData.getID());
                    //pass the user name
                    replyIntent.putExtra("tweetUser", theData.getUser());
                    //go to the tweet screen
                    v.getContext().startActivity(replyIntent);
                    break;
                //retweet button pressed
                case R.id.retweet:
                    //implement retweet

                    new ReTweet(v).execute();

                    break;
                //user has pressed tweet user name
                case R.id.userScreen:
                    //implement visiting user profile
                    //get the user screen name
                    new UserScreen(v,(TextView)v.findViewById(R.id.userScreen)).execute();
                 //   TextView tv = (TextView)v.findViewById(R.id.userScreen);

                    break;
                    default:
                    break;
            }
        }
    };


    private class ReTweet extends AsyncTask<String, Void, Context> {
      View Re_view;
       ReTweet(View v)
       {
           Re_view=v;
       }
        protected Context doInBackground(String... Urls) {
            Context appCont = Re_view.getContext();
            //get preferences for user access
           SharedPreferences tweetPrefs = appCont.getSharedPreferences("TwitNicePrefs", 0);
            String userToken = tweetPrefs.getString("ACCESS_TOKEN", null);
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
            StatusData tweetData = (StatusData)Re_view.getTag();
            try
            {
                //retweet, passing the status ID from the tag
                retweetTwitter.retweetStatus(tweetData.getID());

            }
            catch(TwitterException te) {Log.e(LOG_TAG, te.getMessage());}
            return appCont;
        }

        protected void onPostExecute(Context appCont) {
            //confirm to use
            CharSequence text = "Retweeted!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(appCont, text, duration);
             toast.show();
        }
    }
    private class UserScreen extends AsyncTask<String,String,String>{
        View view_screen;
        TextView text_user_screen;
        public UserScreen(View view,TextView textView){
            view_screen=view;
            text_user_screen=textView;

        }
        @Override
        protected String doInBackground(String... params) {
            String userScreenName = text_user_screen.getText().toString();
            //open the user's profile page in the browser
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://twitter.com/"+userScreenName));
            view_screen.getContext().startActivity(browserIntent);
            return null;
        }
    }

        class LoadPhoto extends AsyncTask<String, String, Bitmap> {

        ImageView profPic;
            public LoadPhoto(ImageView profPic)
            {this.profPic=profPic;}
        protected void onPostExecute(Bitmap image) {
            // TODO Auto-generated method stub

            profPic.setImageBitmap(image);
        }


             @Override
        protected Bitmap doInBackground(String... Urls) {
            String url_display= Urls[0];
            Bitmap user_icon_bitmap= null;
            try {
                InputStream in = new URL(url_display).openStream();
                user_icon_bitmap=BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error Inbackround", "in downloading image task "+e.getMessage());
                e.printStackTrace();
            }
            return user_icon_bitmap;

        }
    }

}
