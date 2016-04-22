package com.example.tweets.sharingtwit;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;


public class ProfileTimelineFragment extends Fragment{
    Bitmap bitmap;
    ImageView click_edit,search;
    MainActivity main;
    /**main view for the home timeline*/
    ListView homeTimeline_list;
    /**database helper for update data*/
    NiceDataHelper timelineHelper;
    /**update database*/
 SQLiteDatabase timelineDB=null;
    /**cursor for handling data*/
 Cursor timelineCursor;
    //logcat variable
     String LOG_TAG="ProfileFragment";
    /**adapter for mapping data*/
   UpdateAdapter timelineAdapter;
    MainActivity mainActivity;
    BroadcastReceiver niceStatusReceiver;
    SharedPreferences pref;
    ImageView profile_mine;
    TextView my_twit_name;
   //int count_tweet;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timeline,container, false);
        Log.e(LOG_TAG,"inside the profile fragment");
       // main=new MainActivity();
       /* Intent i = getActivity().getIntent();
        String fragmentClass = i.getStringExtra("profile_fragment");
        if (!TextUtils.isEmpty(fragmentClass)) {
            Fragment toDisplay = Fragment.instantiate(getActivity(), fragmentClass);
           getFragmentManager()
                    .beginTransaction()
                    .add(R.layout.timeline, toDisplay, null)
                    .commit();}*/
        click_edit= (ImageView) view.findViewById(R.id.click_newTweets);
        search=(ImageView)view.findViewById(R.id.search);
        //setup onclick listener for tweet button
        LinearLayout tweetClicker = (LinearLayout)view.findViewById(R.id.tweetbtn);
        //tweetClicker.setOnClickListener(this);
        pref = getActivity().getSharedPreferences("TwitNicePrefs", Context.MODE_PRIVATE);
        profile_mine = (ImageView)view.findViewById(R.id.profile_mine_image);
        //Logout= (TextView) view.findViewById(R.id.Logout_text);
        my_twit_name= (TextView) view.findViewById(R.id.my_twitter_name);
        homeTimeline_list = (ListView)view.findViewById(R.id.homeList);
        //click_edit.setOnClickListener(getActivity());
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),Search.class));
            }
        });
        tweetClicker.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
            //launch tweet activity
            startActivity(new Intent(getActivity(), NiceTweet.class));
        }
        });

         new LoadProfile().execute();
       try {

            //instantiate database helper// refer this maybe it will return null
            timelineHelper = new NiceDataHelper(getActivity());

            //get the database
            timelineDB = timelineHelper.getReadableDatabase();
            //query the database, most recent tweets first
            //after getting all values it will store to cursor object.
            timelineCursor = timelineDB.query
                    ("twit", null, null, null, null, null, "update_time DESC");
            //manage the updates using a cursor

            if (timelineCursor != null)
                timelineCursor.moveToFirst();
            //instantiate adapter
            // have to see this line and familiar with cursor adpater
            timelineAdapter = new UpdateAdapter(getActivity(), timelineCursor,0);
            //this will make the app populate the new update data in the timeline view
            homeTimeline_list.setAdapter(timelineAdapter);
            //instantiate receiver class for finding out when new updates are available

            scheduleAlarmReceiver();
    }
      catch(Exception te) { Log.e(LOG_TAG, "Failed here from Profile Fragment: " + te.getMessage()); }

      click_edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                timelineCursor = timelineDB.query("twit", null, null, null, null, null, "update_time DESC");
                if (timelineCursor != null)
                    timelineCursor.moveToFirst();
                timelineAdapter = new UpdateAdapter(getActivity(), timelineCursor,0);
                Log.e("Running Receive","Receive register");
                homeTimeline_list.setAdapter(timelineAdapter);
            }
        });
        return view;
    }
    private void scheduleAlarmReceiver() {
        AlarmManager alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(getActivity(), 0, new Intent(getActivity(),TwitterUpdateReceiver.class),
                        PendingIntent.FLAG_CANCEL_CURRENT);

        // Use inexact repeating which is easier on battery (system can phase events and not wake at exact times)
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 30000 ,300000 ,  pendingIntent);
     //   Log.e("time"," "+SystemClock.elapsedRealtime() + 60000);
    }


    private class LoadProfile extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap image) {
            if(image!=null) {
                Bitmap image_circle = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                //prepare a paint with shader
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Paint paint = new Paint();
                paint.setShader(shader);
                Canvas c = new Canvas(image_circle);
                c.drawCircle(image.getWidth() / 2, image.getHeight() / 2, image.getWidth() / 2, paint);
                profile_mine.setImageBitmap(image_circle);
                //prof_name.setText("Welcome " +pref.getString("NAME", ""));
            }
            else
            {
                Toast.makeText(getActivity(),"Network error",Toast.LENGTH_LONG).show();
            }
        }


        @Override
        protected Bitmap doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            try {

                bitmap = BitmapFactory.decodeStream((InputStream) new URL(pref.getString("IMAGE_URL", "")).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            //remove receiver register
           Log.e("ondestroy","db close");
           getActivity().unregisterReceiver(niceStatusReceiver);
            //close the database
            timelineDB.close();
        }
        catch(Exception se) { Log.e(LOG_TAG, "unable to stop Service or receiver"); }
    }

}
