package com.pedometrak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.pedometrak.data.SessionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocalDatabaseManager extends SQLiteOpenHelper {

    private final static String DB_NAME = "metrics";
    private final static int DB_VERSION = 2;

    private static LocalDatabaseManager instance;

    private LocalDatabaseManager(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized LocalDatabaseManager getInstance(final Context c) {
        if (instance == null) {
            instance = new LocalDatabaseManager(c.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DB_NAME + " (start INTEGER, end INTEGER, steps INTEGER, distance REAL, calories REAL, sync INTEGER)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("CREATE TABLE " + DB_NAME + " (start INTEGER, end INTEGER, steps INTEGER, distance REAL, calories REAL, sync INTEGER)");
        }
    }

    /**
     * Get average of all session lengths in database.
     *
     * @return number of steps from the query
     */
    public int queryAverageSessionTime() {
        Cursor c = getReadableDatabase().rawQuery("SELECT ((SUM(start)-SUM(end))/COUNT(*)) FROM metrics", null);

        int result = -1;
        if (c.moveToFirst()) {
            result = c.getInt(0);
        }
        c.close();
        return result;
    }

    /**
     * Get sum of all steps in database.
     *
     * @return number of steps from the query
     */
    public int queryAverageCalories() {
        Cursor c = getReadableDatabase().rawQuery("SELECT AVG(calories) FROM metrics", null);

        int result = -1;
        if (c.moveToFirst()) {
            result = c.getInt(0);
        }
        c.close();
        return result;
    }

    /**
     * Get sum of all steps in database.
     *
     * @return number of steps from the query
     */
    public int queryTotalSteps() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(steps) FROM metrics", null);

        int result = -1;
        if (c.moveToFirst()) {
             result = c.getInt(0);
        }
        c.close();
        return result;
    }

    /**
     * Get sum of all distances in database.
     *
     * @return number of steps from the query
     */
    public int queryTotalDistance() {
        Cursor c = getReadableDatabase().rawQuery("SELECT SUM(distance) FROM metrics", null);

        int result = -1;
        if (c.moveToFirst()) {
            result = c.getInt(0);
        }
        c.close();
        return result;
    }

    /**
     * Set the sync column to true.
     */
    public void setSyncFlag(long startTime) {
        Cursor c = getReadableDatabase().rawQuery(
                "UPDATE metrics SET sync=1 WHERE start=" + Objects.toString(startTime, "0"), null);
        c.close();
    }

    /**
     * Query the 'metrics' table. Remember to close the cursor!
     *
     * @param columns       the columns to query
     * @param selection     the selection
     * @param selectionArgs the selection arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return the cursor
     */
    public Cursor query(final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        return getReadableDatabase().query(
                DB_NAME, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    /**
     * Inserts a new entry in the database.
     * @param start  the date in ms since 1970
     * @param end  the date in ms since 1970
     * @param steps the number of steps taken in session
     * @param distance the distance travelled in session
     * @param calories the number of calories burned
     */
    public void insertSession(long start, long end, int steps, float distance, float calories) {
        getWritableDatabase().beginTransaction();
        try {
            ContentValues values = new ContentValues();

            values.put("start", start);
            values.put("end", end);
            values.put("steps", steps);
            values.put("distance", distance);
            values.put("calories", calories);
            values.put("sync", 0);

            getWritableDatabase().insert(DB_NAME, null, values);
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }

    public List<SessionData> getUnsynchedSessions() {
        List<SessionData> data = new ArrayList<>(100);
        Cursor c = getReadableDatabase().rawQuery("select * from metrics", null);

        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                int sync = c.getInt(5);
                if (sync == 0) {
                    SessionData row = new SessionData();
                    row.start = c.getLong(0);
                    row.end = c.getLong(1);
                    row.steps = c.getInt(2);
                    row.distance = c.getFloat(3);
                    row.calories = c.getFloat(4);
                    data.add(row);
                }
                c.moveToNext();
            }
        }
        c.close();
        return data;
    }
}
