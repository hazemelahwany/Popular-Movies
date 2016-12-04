package com.example.android.popularmovies;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class FavouritesDB extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String FAVOURITES_TABLE_NAME = "favouriteMovies";
    public static final String FAVOURITES_COLUMN_ID = "id";
    public static final String FAVOURITES_COLUMN_MOVIE = "movie";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "create table favourites " +
                        "(id integer primary key, movie text)"
        );
    }

    public FavouritesDB(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS favourites");
        onCreate(sqLiteDatabase);
    }
    public boolean insertMovie (String id, String mov) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("movie", mov);
        db.insert("favourites", null, contentValues);
        return true;
    }
    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from favourites where id="+id+"", null );
        return res;
    }
    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FAVOURITES_TABLE_NAME);
        return numRows;
    }

    public boolean updateMovie (Integer id, String mov) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("movie", mov);
        db.update("favourites", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteMovie (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("favourites",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllMovies() {
        ArrayList<String> array_list = new ArrayList<>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from favourites", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(FAVOURITES_COLUMN_MOVIE)));
            res.moveToNext();
        }
        return array_list;
    }
}
