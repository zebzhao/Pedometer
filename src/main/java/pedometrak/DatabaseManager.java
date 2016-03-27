/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pedometrak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {

    private final static String DB_NAME = "metrics";
    private final static int DB_VERSION = 3;

    private static DatabaseManager instance;

    private DatabaseManager(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DatabaseManager getInstance(final Context c) {
        if (instance == null) {
            instance = new DatabaseManager(c.getApplicationContext());
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
     * Query the 'metrics'. Extract steps columns.
     *
     * @param selection     the selection
     * @param selectionArgs the selection arguments
     * @param groupBy       the group by statement
     * @param having        the having statement
     * @param orderBy       the order by statement
     * @return number of steps from the query
     */
    public int querySteps(final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        Cursor c = getReadableDatabase().query(
                DB_NAME, new String[] {"steps"}, selection, selectionArgs, groupBy, having, orderBy, limit);

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

            getWritableDatabase().insert(DB_NAME, null, values);
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
    }
}
