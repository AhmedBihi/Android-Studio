package com.example.carl.mdhschemaapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

class StoreData extends SQLiteOpenHelper {

    private static final String COURSE_COLUMN = "course";
    private static final String COURSECODE_COLUMN = "coursecode";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String PLACE_COLUMN = "place";
    private static final String START_TIME_COLUMN = "startTime";
    private static final String END_TIME_COLUMN = "endTime";
    private static final String WEEK_COLUMN = "week";

    //for the second table
    private static final String COURSENAME_COLUMN = "coursename";

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "SCHEMA_DATA";
    private static final String SCHEDULE_TABLE_NAME = "SCHEDULE_DATA";
    private static final String COURSES_TABLE_NAME = "COURSENAMES_DATA";

    private static final String SCHEDULE_TABLE_CREATE =
            "CREATE TABLE " + SCHEDULE_TABLE_NAME + " (" +
                    COURSE_COLUMN + " TEXT, " +
                    COURSECODE_COLUMN + " TEXT, " +
                    START_TIME_COLUMN + " TEXT, " +
                    END_TIME_COLUMN + " TEXT, " +
                    DESCRIPTION_COLUMN + " TEXT, " +
                    WEEK_COLUMN + " INTEGER, " +
                    PLACE_COLUMN + " TEXT);";

    private static final String COURSES_TABLE_CREATE =
            "CREATE TABLE " + COURSES_TABLE_NAME + " (" +
                    COURSENAME_COLUMN + " TEXT, " +
                    COURSECODE_COLUMN + " TEXT);";


    public StoreData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SCHEDULE_TABLE_CREATE);
        db.execSQL(COURSES_TABLE_CREATE);
    }

    //for the CourseInfo Object
    public void insertRecord(CourseInfo c) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //put all the data in a ContentValue
        values.put(COURSENAME_COLUMN,c.courseName);
        values.put(COURSECODE_COLUMN,c.courseCode);

        database.insert(
                COURSES_TABLE_NAME,
                null,
                values);

        database.close();
    }

    public void insertRecord(CardInfo c) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //put all the data in a ContentValue
        values.put(COURSE_COLUMN,c.course);
        values.put(COURSECODE_COLUMN,c.courseCode);
        values.put(START_TIME_COLUMN,c.startTime);
        values.put(END_TIME_COLUMN,c.endTime);
        values.put(PLACE_COLUMN,c.Place);
        values.put(DESCRIPTION_COLUMN,c.description);
        values.put(WEEK_COLUMN,c.week);

        database.insert(
               SCHEDULE_TABLE_NAME,
               null,
               values);

        database.close();
    }

    public Cursor getCoursesData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] test = {COURSENAME_COLUMN,
                         COURSECODE_COLUMN,};
        return db.query(COURSES_TABLE_NAME, test, null, null, null, null, null);
    }

    public Cursor getCardInfoData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] test = {
                COURSE_COLUMN,
                COURSECODE_COLUMN,
                START_TIME_COLUMN,
                END_TIME_COLUMN,
                DESCRIPTION_COLUMN,
                PLACE_COLUMN,
                WEEK_COLUMN};
        return db.query(SCHEDULE_TABLE_NAME,test,null,null,null,null,START_TIME_COLUMN);
    }

    public List<CardInfo> convertToCardInfo()
    {
        List<CardInfo> result = new ArrayList<>();
        Cursor cursor = getCardInfoData();

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            CardInfo ci = new CardInfo();
            ci.course = cursor.getString(cursor.getColumnIndexOrThrow(COURSE_COLUMN));
            ci.courseCode = cursor.getString(cursor.getColumnIndexOrThrow(COURSECODE_COLUMN));
            ci.startTime = cursor.getString(cursor.getColumnIndexOrThrow(START_TIME_COLUMN));
            ci.endTime = cursor.getString(cursor.getColumnIndexOrThrow(END_TIME_COLUMN));
            ci.description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COLUMN));
            ci.Place = cursor.getString(cursor.getColumnIndexOrThrow(PLACE_COLUMN));
            ci.week = cursor.getInt(cursor.getColumnIndexOrThrow(WEEK_COLUMN));

            result.add(ci);
        }

        cursor.close();

        return result;
    }

    public List<CardInfo> convertToCardInfo(int week)
    {
        List<CardInfo> result = new ArrayList<>();
        List<CardInfo> allData = convertToCardInfo();

        for (int i = 0; i < allData.size() && week != 0; i++)
        {
            if(allData.get(i).week == week)
            {
                result.add(allData.get(i));
            }
        }
        return result;
    }

    public List<CourseInfo> convertToCourseInfo()
    {
        List<CourseInfo> result = new ArrayList<>();
        Cursor cursor = getCoursesData();

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext())
        {
            CourseInfo ci = new CourseInfo();
            ci.courseName = cursor.getString(cursor.getColumnIndexOrThrow(COURSENAME_COLUMN));
            ci.courseCode = cursor.getString(cursor.getColumnIndexOrThrow(COURSECODE_COLUMN));
            result.add(ci);
        }

        cursor.close();
        return result;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public int getAmountOfWeeks() {
        List<CardInfo> allWeeks = convertToCardInfo();
        if(allWeeks.isEmpty())
            return 0;

        return allWeeks.get(allWeeks.size() -1).week - allWeeks.get(0).week;
    }

    public void delete() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(SCHEDULE_TABLE_NAME, null, null);
    }

    public void deleteCourses() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(COURSES_TABLE_NAME, null, null);
    }

    public int getFirstWeek() {
        List<CardInfo> allWeeks = convertToCardInfo();
        if(allWeeks.isEmpty())
            return 0;

        return allWeeks.get(0).week;
    }
}
