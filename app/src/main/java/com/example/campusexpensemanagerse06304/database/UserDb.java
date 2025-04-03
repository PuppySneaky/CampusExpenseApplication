package com.example.campusexpensemanagerse06304.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.campusexpensemanagerse06304.model.Users;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class UserDb extends SQLiteOpenHelper {

    private static final String DB_NAME = "campus expenses";
    private static final String TABLE_NAME = "user";
    private static final int DB_VERSION = 1;
    //create col for table
    private static final String ID_COL = "id";
    private static final String USERNAME_COL = "username";
    private static final String PASSWORD_COL = "password";
    private static final String EMAIL_COL = "email";
    private static final String PHONE_COL = "phone";
    private static final String ROLE_ID_COL = "role_id";
    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";
    private static final String DELETED_AT = "deleted_at";
    public UserDb(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       //create table
        String query = "CREATE TABLE " + TABLE_NAME + " ( "
                        + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + USERNAME_COL + " VARCHAR(60) NOT NULL, "
                        + PASSWORD_COL + " VARCHAR(200) NOT NULL, "
                        + EMAIL_COL + " VARCHAR(60) NOT NULL, "
                        + PHONE_COL + " VARCHAR(30), "
                        + ROLE_ID_COL + " INTEGER, "
                        + CREATED_AT + " DATETIME, "
                        + UPDATED_AT + " DATETIME, "
                        + DELETED_AT + " DATETIME ) ";
        db.execSQL(query); // thuc thi tao bang
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertUserAccount(String username, String password, String email, String phone){
        // xu li lay thoi gian hien tai
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZonedDateTime zoneDt = ZonedDateTime.now();
        String currentDate = dtf.format(zoneDt);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME_COL, username);
        values.put(PASSWORD_COL, password);
        values.put(EMAIL_COL, email);
        values.put(PHONE_COL, phone);
        values.put(ROLE_ID_COL, 1);
        values.put(CREATED_AT, currentDate);
        long insert = db.insert(TABLE_NAME, null, values);
        db.close();
        return insert;
    }

    //check var login
    @SuppressLint("Range")
    public Users checkLoginUser(String username, String password){
        Users users = new Users();
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            // SELECT id, username, email, phone, roleId from users where username = ? and password = ?
            String[] cols = { ID_COL, USERNAME_COL, EMAIL_COL, PHONE_COL, ROLE_ID_COL} ;
            String condition = USERNAME_COL + " =? AND " + PASSWORD_COL + " =? ";
            String[] params = { username, password};
            Cursor cursor = db.query(TABLE_NAME, cols, condition, params, null, null, null);
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                //anh xa du lieu database vao model
                users.setId(cursor.getInt(cursor.getColumnIndex(ID_COL)));
                users.setUsername(cursor.getString(cursor.getColumnIndex(USERNAME_COL)));
                users.setEmail(cursor.getString(cursor.getColumnIndex(EMAIL_COL)));
                users.setPhone(cursor.getString(cursor.getColumnIndex(PHONE_COL)));
                users.setRoleId(cursor.getInt(cursor.getColumnIndex(ROLE_ID_COL)));
            }
            cursor.close();
            db.close();
        } catch (RuntimeException e){
            throw new RuntimeException(e);
        }
        return users;
    }

    public boolean checkExistsUsername(String username, String email){
        boolean checking = false;
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            String[] cols = {ID_COL, USERNAME_COL, EMAIL_COL};
            String condition = USERNAME_COL + " =? AND " + EMAIL_COL + " =? ";
            String[] params = {username, email};
            Cursor cursor = db.query(TABLE_NAME, cols, condition, params, null, null, null);
            if(cursor.getCount() > 0){
                checking = true;
            }
            cursor.close();
            db.close();
        }catch (RuntimeException e){
            throw new RuntimeException(e);
        }return checking;
    }
    public int changePassword(String newPassword, String account, String email){
        int check = -1;
        try{
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PASSWORD_COL, newPassword);
            String condition = USERNAME_COL + " =? AND "+ EMAIL_COL+" =? ";
            String[] params = { account , email };
            check = db.update(TABLE_NAME, values, condition, params);
            db.close();
        }catch (RuntimeException e){
            throw new RuntimeException(e);
        }
        return check;
    }
}






