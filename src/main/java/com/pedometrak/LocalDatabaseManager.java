package com.pedometrak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDatabaseManager extends SQLiteOpenHelper {

    private final static String DB_NAME = "metrics";
    private final static int DB_VERSION = 3;

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
        db.execSQL("CREATE TABLE " + DB_NAME + " (start INTEGER, end INTEGER, steps INTEGER, distance REAL, calories REAL)");
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE " + DB_NAME);
            db.execSQL("CREATE TABLE " + DB_NAME + " (start INTEGER, end INTEGER, steps INTEGER, distance REAL, calories REAL)");
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
    public void insertRecord(long start, long end, int steps, float distance, float calories) {
        getWritableDatabase().beginTransaction();
        try {
            ContentValues values = new ContentValues();

            values.put("start", start);
            values.put("end", end);
            values.put("steps", steps);
            values.put("distance", distance);
            values.put("calories", calories);
            values.put("sync", false);

            getWritableDatabase().insert(DB_NAME, null, values);
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }
}
