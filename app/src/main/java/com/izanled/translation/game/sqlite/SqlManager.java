package com.izanled.translation.game.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.izanled.translation.game.data.TranslationData;

public class SqlManager {
    static SqlManager m_sharedInstance = null;
    private Context mContext;
    public DBManager mDb;

    public SqlManager(){}
    public SqlManager(Context context, DBManager db){
        this.mContext = context;
        mDb = db;
    }

    public static SqlManager shared()
    {
        synchronized(SqlManager.class)
        {
            if(m_sharedInstance == null)
                m_sharedInstance = new SqlManager();
        }
        return m_sharedInstance;
    }

    public static SqlManager shared(Context context, DBManager db)
    {
        synchronized(SqlManager.class)
        {
            if(m_sharedInstance == null)
                m_sharedInstance = new SqlManager(context, db);
        }
        return m_sharedInstance;
    }

    public TranslationData getTranslationData(String original, String source, String target){
        SQLiteDatabase db = mDb.getReadableDatabase();
        TranslationData data = null;

        try{
            Cursor cursor = db.rawQuery(" SELECT * FROM transfer_table WHERE original = '" + original + "' AND target = '" + target +"' AND source = '" + source + "' Order by _id DESC;", null);
            if( cursor.moveToFirst()){
                data = new TranslationData();
                data.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
                data.setOriginal(cursor.getString(cursor.getColumnIndex("original")));
                data.setTranslation(cursor.getString(cursor.getColumnIndex("translation")));
                data.setCount(cursor.getInt(cursor.getColumnIndex("count")));
                data.setTarget(cursor.getString(cursor.getColumnIndex("target")));
                data.setSource(cursor.getString(cursor.getColumnIndex("source")));
            }
            db.close();

            return data;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void insertTranslation(TranslationData insertData){
        SQLiteDatabase db = mDb.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("original",  insertData.getOriginal());
        values.put("translation",  insertData.getTranslation());
        values.put("count",  insertData.getCount());
        values.put("target",  insertData.getTarget());
        values.put("source",  insertData.getSource());

        long index = db.insert("transfer_table", null, values);
        db.close();

        insertData.set_id((int)index);
    }

    public void updateTranslation(TranslationData insertData){
        SQLiteDatabase db = mDb.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("original",  insertData.getOriginal());
        values.put("translation",  insertData.getTranslation());
        values.put("count",  insertData.getCount());
        values.put("target",  insertData.getTarget());
        values.put("source",  insertData.getSource());

        db.update("transfer_table", values, "_Id=?", new String[]{String.valueOf(insertData.get_id())});

        db.close();
    }

}
