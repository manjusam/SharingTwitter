package com.example.tweets.sharingtwit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.ListView;

/**
 * Created by admin on 13/08/2015.
 */
public  class TwitterUpdateReceiver extends BroadcastReceiver {
    // int get_count;
    NiceDataHelper timelineHelper;
    /**update database*/
    SQLiteDatabase timelineDB=null;
    /**cursor for handling data*/
    Cursor timelineCursor;
    //logcat variable
    String LOG_TAG="ProfileFragment";
    /**adapter for mapping data*/
    UpdateAdapter timelineAdapter;


    public void onReceive(Context context, Intent intent) {
        Log.i("TWITTER PROFILE", "Twitter update class invoked");
        Intent service_intent=  new Intent(context,TimelineServices.class);
       context.startService(service_intent);
        try{
        timelineHelper = new NiceDataHelper(context);

        //get the database
        timelineDB = timelineHelper.getReadableDatabase();


        }

        catch(Exception te) { Log.e("DELETING ", "DELETING FROM SEPAREATE RECEIVER CLASS: " + te.getMessage()); }
        int rowLimit = 100;

        // get_count=intent.getIntExtra("tweet_count",0);
        // Log.e("get count","count"+""+get_count);
        if(DatabaseUtils.queryNumEntries(timelineDB, "twit")>rowLimit) {
            Log.e("Running Receive","Rowlimit is high");
            String deleteQuery = "DELETE FROM twit WHERE "+ "twitter_id" + " NOT IN " +
                    "(SELECT "+ "twitter_id" +" FROM twit ORDER BY "+" update_time DESC " +
                    "limit "+rowLimit+")";
            //String deleteQuery=
            timelineDB.execSQL(deleteQuery);

        }
        // createNotification();

    }


}