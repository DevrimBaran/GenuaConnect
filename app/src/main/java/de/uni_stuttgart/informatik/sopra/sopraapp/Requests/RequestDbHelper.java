package de.uni_stuttgart.informatik.sopra.sopraapp.Requests;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class RequestDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "Requests.db";

    //sql command to destroy request table
    private static final String SQL_DELETE_REQUESTS =
            "DROP TABLE IF EXISTS " + RequestsContract.REQ_TABLE_NAME;

    //sql command to destroy oid table
    private static final String SQL_DELETE_OID =
            "DROP TABLE IF EXISTS " + RequestsContract.OID_TABLE_NAME;

    public RequestDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //sql command to create requestmask table(for the mask not oids)
        final String SQL_CREATE_REQUESTS =
                "CREATE TABLE " + RequestsContract.REQ_TABLE_NAME + " (" +
                        RequestsContract.COLUMN_REQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        RequestsContract.COLUMN_REQ_NAME + " TEXT);";


        final String SQL_CREATE_OIDS =
                "CREATE TABLE " + RequestsContract.OID_TABLE_NAME + " (" +
                        RequestsContract.COLUMN_OID_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        RequestsContract.COLUMN_OID_STRING + " TEXT, " +
                        RequestsContract.COLUMN_OID_REQ + " INTEGER);";

        db.execSQL(SQL_CREATE_REQUESTS);
        db.execSQL(SQL_CREATE_OIDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_REQUESTS);
        db.execSQL(SQL_DELETE_OID);
        onCreate(db);
    }

    public ArrayList<String> getAllMasks(){
        SQLiteDatabase reading = getReadableDatabase();
        ArrayList<String> masks = new ArrayList<>();
        Cursor cursor = reading.rawQuery("select * from " + RequestsContract.REQ_TABLE_NAME,null);
        String mask;
        for(int i = 1; i<=cursor.getCount(); i++ ){
            mask = cursor.getString(cursor.getColumnIndex(RequestsContract.COLUMN_REQ_NAME));
            masks.add(mask);
        }
        cursor.close();
        return masks;
    }

    public ArrayList<String> getOIDsFrom(String request){
        SQLiteDatabase reading = getReadableDatabase();

        Cursor cursor = reading.rawQuery("select * from " + RequestsContract.REQ_TABLE_NAME + " where " + RequestsContract.COLUMN_REQ_NAME + " = '" + request+"'" ,null);
        cursor.moveToFirst();
        int id = cursor.getInt(cursor.getColumnIndex(RequestsContract.COLUMN_REQ_ID));

        ArrayList<String> oids = new ArrayList<>();
        cursor = reading.rawQuery("select * from " + RequestsContract.OID_TABLE_NAME + " where "+ RequestsContract.COLUMN_OID_REQ + " = " + id, null);
        String oid;
        for(int i = 1; i<=cursor.getCount(); i++ ){
            oid = cursor.getString(cursor.getColumnIndex(RequestsContract.COLUMN_REQ_NAME));
            oids.add(oid);
        }
        cursor.close();
        return oids;
    }
}
