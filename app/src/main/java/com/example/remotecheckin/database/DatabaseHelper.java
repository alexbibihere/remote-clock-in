package com.example.remotecheckin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.remotecheckin.model.CheckInRecord;
import com.example.remotecheckin.model.LocationPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理类
 * 管理位置点和打卡记录
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "remote_checkin.db";
    private static final int DATABASE_VERSION = 1;

    // 位置点表
    private static final String TABLE_LOCATION_POINTS = "location_points";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_ACCURACY = "accuracy";
    private static final String COL_TIMESTAMP = "timestamp";

    // 打卡记录表
    private static final String TABLE_CHECKIN_RECORDS = "checkin_records";
    private static final String COL_LOCATION_POINT_ID = "location_point_id";
    private static final String COL_LOCATION_NAME = "location_name";
    private static final String COL_SCHEDULED_TIME = "scheduled_time";
    private static final String COL_ACTUAL_TIME = "actual_time";
    private static final String COL_SUCCESS = "success";
    private static final String COL_ERROR_MESSAGE = "error_message";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建位置点表
        String createLocationTable = "CREATE TABLE " + TABLE_LOCATION_POINTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_LATITUDE + " REAL NOT NULL, " +
                COL_LONGITUDE + " REAL NOT NULL, " +
                COL_ACCURACY + " REAL DEFAULT 10.0, " +
                COL_TIMESTAMP + " INTEGER" +
                ")";

        // 创建打卡记录表
        String createCheckInTable = "CREATE TABLE " + TABLE_CHECKIN_RECORDS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LOCATION_POINT_ID + " INTEGER, " +
                COL_LOCATION_NAME + " TEXT, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL, " +
                COL_SCHEDULED_TIME + " INTEGER, " +
                COL_ACTUAL_TIME + " INTEGER, " +
                COL_SUCCESS + " INTEGER DEFAULT 0, " +
                COL_ERROR_MESSAGE + " TEXT, " +
                "FOREIGN KEY(" + COL_LOCATION_POINT_ID + ") REFERENCES " + TABLE_LOCATION_POINTS + "(" + COL_ID + ")" +
                ")";

        db.execSQL(createLocationTable);
        db.execSQL(createCheckInTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKIN_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_POINTS);
        onCreate(db);
    }

    // ==================== 位置点操作 ====================

    /**
     * 添加位置点
     */
    public long addLocationPoint(LocationPoint location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, location.getName());
        values.put(COL_LATITUDE, location.getLatitude());
        values.put(COL_LONGITUDE, location.getLongitude());
        values.put(COL_ACCURACY, location.getAccuracy());
        values.put(COL_TIMESTAMP, location.getTimestamp());

        long id = db.insert(TABLE_LOCATION_POINTS, null, values);
        db.close();

        return id;
    }

    /**
     * 获取位置点
     */
    public LocationPoint getLocationPoint(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_LOCATION_POINTS,
                null,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        LocationPoint location = null;
        if (cursor != null && cursor.moveToFirst()) {
            location = cursorToLocationPoint(cursor);
            cursor.close();
        }

        db.close();
        return location;
    }

    /**
     * 获取所有位置点
     */
    public List<LocationPoint> getAllLocationPoints() {
        List<LocationPoint> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_LOCATION_POINTS,
                null,
                null, null, null, null,
                COL_TIMESTAMP + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                locations.add(cursorToLocationPoint(cursor));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return locations;
    }

    /**
     * 更新位置点
     */
    public int updateLocationPoint(LocationPoint location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_NAME, location.getName());
        values.put(COL_LATITUDE, location.getLatitude());
        values.put(COL_LONGITUDE, location.getLongitude());
        values.put(COL_ACCURACY, location.getAccuracy());

        int rows = db.update(
                TABLE_LOCATION_POINTS,
                values,
                COL_ID + " = ?",
                new String[]{String.valueOf(location.getId())}
        );

        db.close();
        return rows;
    }

    /**
     * 删除位置点
     */
    public void deleteLocationPoint(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION_POINTS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    // ==================== 打卡记录操作 ====================

    /**
     * 添加打卡记录
     */
    public long addCheckInRecord(CheckInRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_LOCATION_POINT_ID, record.getLocationPointId());
        values.put(COL_LOCATION_NAME, record.getLocationName());
        values.put(COL_LATITUDE, record.getLatitude());
        values.put(COL_LONGITUDE, record.getLongitude());
        values.put(COL_SCHEDULED_TIME, record.getScheduledTime());
        values.put(COL_ACTUAL_TIME, record.getActualTime());
        values.put(COL_SUCCESS, record.isSuccess() ? 1 : 0);
        values.put(COL_ERROR_MESSAGE, record.getErrorMessage());

        long id = db.insert(TABLE_CHECKIN_RECORDS, null, values);
        db.close();

        return id;
    }

    /**
     * 获取所有打卡记录
     */
    public List<CheckInRecord> getAllCheckInRecords() {
        List<CheckInRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CHECKIN_RECORDS,
                null, null, null, null, null,
                COL_SCHEDULED_TIME + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                records.add(cursorToCheckInRecord(cursor));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return records;
    }

    /**
     * 获取未来的打卡任务
     */
    public List<CheckInRecord> getUpcomingCheckIns() {
        List<CheckInRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        long currentTime = System.currentTimeMillis();
        Cursor cursor = db.query(
                TABLE_CHECKIN_RECORDS,
                null,
                COL_SCHEDULED_TIME + " > ?",
                new String[]{String.valueOf(currentTime)},
                null, null,
                COL_SCHEDULED_TIME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                records.add(cursorToCheckInRecord(cursor));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return records;
    }

    /**
     * 获取指定位置点的打卡记录
     */
    public List<CheckInRecord> getCheckInRecordsByLocation(long locationPointId) {
        List<CheckInRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_CHECKIN_RECORDS,
                null,
                COL_LOCATION_POINT_ID + " = ?",
                new String[]{String.valueOf(locationPointId)},
                null, null,
                COL_SCHEDULED_TIME + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                records.add(cursorToCheckInRecord(cursor));
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return records;
    }

    /**
     * 删除打卡记录
     */
    public void deleteCheckInRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHECKIN_RECORDS, COL_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    /**
     * 清空所有打卡记录
     */
    public void clearAllCheckInRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CHECKIN_RECORDS, null, null);
        db.close();
    }

    // ==================== 辅助方法 ====================

    private LocationPoint cursorToLocationPoint(Cursor cursor) {
        LocationPoint location = new LocationPoint();
        location.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        location.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        location.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE)));
        location.setAccuracy(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_ACCURACY)));
        location.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TIMESTAMP)));
        return location;
    }

    private CheckInRecord cursorToCheckInRecord(Cursor cursor) {
        CheckInRecord record = new CheckInRecord();
        record.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
        record.setLocationPointId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCATION_POINT_ID)));
        record.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION_NAME)));
        record.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE)));
        record.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE)));
        record.setScheduledTime(cursor.getLong(cursor.getColumnIndexOrThrow(COL_SCHEDULED_TIME)));
        record.setActualTime(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ACTUAL_TIME)));
        record.setSuccess(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUCCESS)) == 1);
        record.setErrorMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_ERROR_MESSAGE)));
        return record;
    }
}
