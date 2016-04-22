package com.example.tweets.sharingtwit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.widget.ListView;

public class MainActivity extends Activity {
    SharedPreferences pref;
    /**Broadcast receiver for when new updates are available*/

    /**database helper for update data*/
    static NiceDataHelper timelineHelper;
    /**update database*/
   static  SQLiteDatabase timelineDB;
    /**cursor for handling data*/
       static  Cursor timelineCursor;
    /**adapter for mapping data*/
   static UpdateAdapter timelineAdapter;
    /**main view for the home timeline*/
    static ListView homeTimeline;

    //logcat variable
    private String LOG_TAG;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //instantiate database helper
        timelineHelper = new NiceDataHelper(this);

        //get the database
        timelineDB = timelineHelper.getReadableDatabase();
        pref = getSharedPreferences("TwitNicePrefs", Context.MODE_PRIVATE);
        homeTimeline= (ListView) findViewById(R.id.homeList);
        // niceStatusReceiver  = new TwitterUpdateReceiver();
        if (pref.getString("ACCESS_TOKEN", "").isEmpty()) {
        saveAPItoSharePref();
        Fragment login = new LoginFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, login);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

        }
        else{
            //Intent intent_frag = new Intent();

           // startActivity(new Intent(this, ProfileTimelineFragment.class));
        Fragment profile = new ProfileTimelineFragment();
            FragmentTransaction ft = getFragmentManager()
                    .beginTransaction();
            //.add(R.id.content_frame,profile);
            ft.replace(R.id.content_frame, profile);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    private void saveAPItoSharePref() {
         pref= getSharedPreferences("TwitNicePrefs", Context.MODE_PRIVATE);
       // pref = getPreferences(0);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("CONSUMER_KEY", Config.API_KEY);
        edit.putString("CONSUMER_SECRET", Config.API_SECRET);
        edit.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(new Intent(this, TimelineServices.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            //stop the updater Service
            Log.e("ondestroy","db close" + "");
            stopService(new Intent(this, TimelineServices.class));

            //close the database
            timelineDB.close();
        }
        catch(Exception se) { Log.e(LOG_TAG, "unable to stop Service or receiver"); }
    }
}
