package com.example.tweets.sharingtwit;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TimelineServices extends Service {
    /**twitter developer key*/
    public final static String TWIT_KEY = Config.API_KEY;//alter
    /**twitter developer secret*/
    public final static String TWIT_SECRET = Config.API_SECRET;//alter
    /**twitter object*/
    private Twitter timelineTwitter;
    /**database helper object*/
    NiceDataHelper niceHelper;
    /**timeline database*/
    SQLiteDatabase niceDB= null;
    /**shared preferences for user details*/
    SharedPreferences pref;
    /**handler for updater*/
    Handler niceHandler;
    /**delay between fetching new tweets*/
    String LOG_TAG = "TimelineService";
    /**updater thread object*/
    GetTimeline getTimeline;
    static int count=0;
    @Override
    public void onCreate() {
        super.onCreate();
        //get prefs

        pref = getSharedPreferences("TwitNicePrefs", Context.MODE_PRIVATE);
        //get database helper
        niceHelper = new NiceDataHelper(this);
        //get the database
        niceDB = niceHelper.getWritableDatabase();
        //get user preferences

        String userToken = pref.getString("ACCESS_TOKEN", null);
        Log.e("Service", userToken);
        String userSecret = pref.getString("ACCESS_TOKEN_SECRET", null);
        Log.e("Service",userSecret);

        //create new configuration
        Configuration twitConf = new ConfigurationBuilder()
                .setOAuthConsumerKey(TWIT_KEY)
                .setOAuthConsumerSecret(TWIT_SECRET)
                .setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret)
                .build();
        //instantiate new twitter
        timelineTwitter = new TwitterFactory(twitConf).getInstance();
        //setup the class
    }




    @Override
     public IBinder onBind(Intent intent) {
        return null;
    }
     class GetTimeline extends AsyncTask<String, String,Integer> {
       // boolean   statusChanges=false;
       boolean check_id;

        @Override
        protected Integer doInBackground(String... params) {
                try {
                    //fetch timeline
                    Log.e("inside run","inside timelineupdater run"+"   "+params);

                    //retrieve the new home timeline tweets as a list
                    List<twitter4j.Status> homeTimeline = timelineTwitter.getHomeTimeline();
                    //iterate through new status updates
                    for (twitter4j.Status statusUpdate : homeTimeline) {
                        Log.e("For loop","Home timeline");
                        //call the getValues method of the data helper class, passing the new updates
                        ContentValues timelineValues = NiceDataHelper.getValues(statusUpdate);

                        //String time;
                        check_id = niceHelper.ContainsTweets(statusUpdate.getId());
                        //time=timelineValues();

                        //if the database already contains the updates they will not be inserted
                        // checkid getting
                        if(check_id){
                        count++;
                        niceDB.insertOrThrow("twit", null, timelineValues);
                        }
                        //confirm we have new updates
                        //statusChanges = true;
                    }
                } catch (Exception te) {
                    Log.e(LOG_TAG, "Exception: " + te);
                }
            return count;
        }

        @Override
        protected void onPostExecute(Integer count_tweet) {
            super.onPostExecute(count_tweet);
            if (count_tweet>0) {
             Log.e("inside POST ASYNCTASK","inside POSTEXCUTE");
             //sendBroadcast(intent);
             createNotification(count_tweet);
            }


        }
    }
    Runnable repeatingTask = new Runnable() {
        public void run() {
            Log.e("repeating task","Reapeating Runnable");
            getTimeline= new GetTimeline();
            getTimeline.execute("my String");


        }
    };
    public void createNotification(int get_count) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("New Tweets")
                .setContentText(get_count + " " + "Tweets")
                .setSmallIcon(R.drawable.twit)
                .setContentIntent(pIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)

                .build();
          NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


          notificationManager.notify(0, notification);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags, startId);
        //get handler
        Log.e("onstart command","onstart command");
        niceHandler = new Handler();
       // Toast.makeText(this,"Loading....",Toast.LENGTH_LONG).show();
        //add to run queue
        niceHandler.post(repeatingTask);
        //return sticky
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //stop the updating
        niceHandler.removeCallbacks(repeatingTask);
        niceDB.close();
    }

}