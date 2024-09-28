package com.store.neareststores;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_favorites_db";
    private static final int DATABASE_VERSION = 9;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private boolean databaseExists(Context context) {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_FAVORITES = "CREATE TABLE IF NOT EXISTS favorites ("
                + "id INTEGER PRIMARY KEY, "
                + "name TEXT, "
                + "latitude REAL, "
                + "longitude REAL)";
        String CREATE_TABLE_REVIEWS = "CREATE TABLE IF NOT EXISTS reviews ("
                + "id INTEGER PRIMARY KEY, "
                + "store_name TEXT, "
                + "review TEXT)";
        String CREATE_TABLE_RATINGS = "CREATE TABLE IF NOT EXISTS ratings ("
                + "id INTEGER PRIMARY KEY, "
                + "store_name TEXT, "
                + "rating REAL)";
        try {
            db.execSQL(CREATE_TABLE_FAVORITES);
            db.execSQL(CREATE_TABLE_REVIEWS);
            db.execSQL(CREATE_TABLE_RATINGS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean addRating(String storeName, double rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("store_name", storeName);
        values.put("rating", rating);
        long result = 0;
        try {
            result = db.insert("ratings", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return result != -1;
    }

    public double getRatingForStore(String storeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT rating FROM ratings WHERE store_name = ?";
        Cursor cursor = null;
        double rating = 0.0;
        try {
            cursor = db.rawQuery(query, new String[]{storeName});
            if (cursor.moveToNext()) {
                rating = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return rating;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS favorites");
            db.execSQL("DROP TABLE IF EXISTS reviews");
            db.execSQL("DROP TABLE IF EXISTS ratings");
            onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void removeAllFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            db.delete("favorites", null, null);
            db.delete("reviews", null, null);
            db.delete("ratings", null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error removing all favorites: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    public boolean addReview(String storeName, String review) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("store_name", storeName);
        values.put("review", review);
        long result = 0;
        try {
            result = db.insert("reviews", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return result != -1;
    }

    public boolean removeStoreFromFavorites(String storeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result = false;
        try {
            db.beginTransaction();
            // Delete reviews for the store
            db.delete("reviews", "store_name = ?", new String[]{storeName});
            // Delete ratings for the store
            db.delete("ratings", "store_name = ?", new String[]{storeName});
            // Delete the store from favorites
            int rowsDeleted = db.delete("favorites", "name = ?", new String[]{storeName});
            if (rowsDeleted > 0) {
                db.setTransactionSuccessful();
                result = true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error removing store from favorites: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return result;
    }
    public List<MyReview> getReviewsForStore(String storeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM reviews WHERE store_name = ?";
        Cursor cursor = null;
        List<MyReview> reviews = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, new String[]{storeName});
            while (cursor.moveToNext()) {
                MyReview review = new MyReview(cursor.getString(1), cursor.getString(2));
                reviews.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return reviews;
    }

    public boolean addStoreToFavorites(Store store) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", store.getName());
        values.put("latitude", store.getLatitude());
        values.put("longitude", store.getLongitude());
        long result = db.insert("favorites", null, values);
        db.close();
        return result != -1;
    }

    public List<Store> getFavoriteStores() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM favorites";
        Cursor cursor = null;
        List<Store> stores = new ArrayList<>();
        try {
            cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                Store store = new Store(cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3));
                stores.add(store);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return stores;
    }
}
