package com.example.tweets.sharingtwit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import twitter4j.Status;

/**
 * Created by admin on 20/07/2015.
 */
public class NiceDataHelper extends SQLiteOpenHelper {
    /**db version*/
    private static final int DATABASE_VERSION = 9;
    /**database name*/
    private static final String DATABASE_NAME = "twit.db";
    /**ID column*/
    private static final String HOME_COL =      "_id";
    /**TWIT - ID column*/
    private static final String TWIT_ID =   "twitter_id";
    /**tweet text*/
    private static final String UPDATE_COL = "update_text";
    /**twitter screen name*/
    private static final String USER_COL = "user_screen";
    /**time tweeted*/
    private static final String TIME_COL = "update_time";
    /**user profile image*/
    private static final String USER_IMG = "user_img";
    SQLiteDatabase timelineDB=null;
    /**cursor for handling data*/
    Cursor twitCursor;
    int count=0;
    //logcat variable
    /**database creation string*/
    private static final String DATABASE_CREATE = "CREATE TABLE twit (" + HOME_COL +
            " INTEGER PRIMARY KEY AUTOINCREMENT, " + TWIT_ID + " INTEGER NOT NULL, "  + UPDATE_COL + " TEXT, " + USER_COL +
            " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT);";
   Context context;

    NiceDataHelper(Context context)
    {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //eXCECTING the table creation
        db.execSQL(DATABASE_CREATE);
    }
    /**
     * getValues retrieves the database records
     * - called from TimelineUpdater in TimelineService
     * - this is a static method that can be called without an instance of the class
     *
     * @param status
     * @return ContentValues result
     */
    public static ContentValues getValues(Status status) {

        //prepare ContentValues to return
        ContentValues homeValues = new ContentValues();

        //get the values
        try {
            //get each value from the table
           // homeValues.put(HOME_COL,"");
            homeValues.put(TWIT_ID, status.getId());
            homeValues.put(UPDATE_COL, status.getText());
            homeValues.put(USER_COL, status.getUser().getScreenName());
            homeValues.put(TIME_COL, status.getCreatedAt().getTime());
            Object time =  homeValues.get(TIME_COL);
            Log.e("tt","ti"+ time.toString());
            //status.getUser().getProfileImageURL(); //returns a string which is profile image url
           // homeValues.put(USER_IMG, status.getUser().getProfileImageURL().toString()); is redundant
            homeValues.put(USER_IMG, status.getUser().getProfileImageURL());
        }
        catch(Exception te)
        { Log.e("NiceDataHelper", te.getMessage()); }
        //return the values
        return homeValues;
    }
    public Boolean ContainsTweets(long entryId)
    {

     NiceDataHelper niceData= new NiceDataHelper(context);
     timelineDB = niceData.getReadableDatabase();
       twitCursor = timelineDB.rawQuery("SELECT * FROM twit WHERE twitter_id = '" + entryId + "'", null);
        if (twitCursor.getCount() > 0) { // This will get the number of rows
            return false;
        }
       // count++;
        return true;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      //  db.execSQL("DROP TABLE IF EXISTS home");
        db.execSQL("DROP TABLE IF EXISTS twit");

        onCreate(db);
    }
}
