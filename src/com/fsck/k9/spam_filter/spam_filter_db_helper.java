package com.fsck.k9.spam_filter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

//import com.fsck.k9.K9;

/**
 * Created by AvatarBlueray on 15.02.2015.
 */
public class spam_filter_db_helper  extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "spam_filter_DatabaseHelper";

    public static final String KEY_TITLE = "title";

    public static final String KEY_FROM = "from1";
    public static final String KEY_SUBJ = "subj";
    public static final String KEY_HIDE = "_hide";
    public static final String KEY_DEL = "_del";
    public static final String KEY_ROWID = "_id";

    public static final long  ACTION_ALLOW = 0;
    public static final long  ACTION_HIDE = 1;
    public static final long  ACTION_HIDENDEL = 2;

    // Database Name
    private static final String DATABASE_NAME = "data";

    // Table Names
    private static final String TABLE_NOTES = "notes";
    private static final int DATABASE_VERSION = 4;

    private static final String DATABASE_CREATE =
            "create table notes ("+KEY_ROWID+" integer primary key autoincrement, "
                    + KEY_TITLE + " text not null, "
                    + KEY_FROM  + " text not null, "
                    + KEY_SUBJ  + " text not null, "
                    + KEY_HIDE  + " integer, "
                    + KEY_DEL   + " integer    );";

    public spam_filter_db_helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating required tables
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);

        // create new tables
        onCreate(db);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    private long getReturnValue(long hide, long del, Cursor c){

        c.close();

        if (del!=0){
            return   ACTION_HIDENDEL;
        }else{
            if (hide != 0){
                return   ACTION_HIDE;
            }else{
                return  ACTION_ALLOW;
            }
        }

    }
    public long checkForSpamGetAction(String from, String subj) {

        String selectQuery = "SELECT  * FROM " + TABLE_NOTES;

       // Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {

                String from_t = (c.getString((c.getColumnIndex(KEY_FROM))));
                String subj_t = (c.getString((c.getColumnIndex(KEY_SUBJ))));

              //  Log.e(K9.LOG_TAG, "check for spam " + from_t + "|"+from + " "+subj_t +"|"+subj );

                long hide = (c.getLong((c.getColumnIndex(KEY_HIDE))));
                long del = (c.getLong((c.getColumnIndex(KEY_DEL))));

                if ( ! from_t.isEmpty()){
                    if ( from.matches(from_t) ){
                        if (! subj_t.isEmpty() ){
                            if (subj.matches(subj_t)){
                                return  getReturnValue(hide,del,c);
                            }
                        }else{
                            return  getReturnValue(hide,del,c);
                        }
                    }
                }else{
                    if (! subj_t.isEmpty()){
                        if (subj.matches(subj_t)){
                            return  getReturnValue(hide,del,c);
                        }
                    }
                }

            } while (c.moveToNext());
        }
        c.close();
        return ACTION_ALLOW;
    }

}
