package it.danieleverducci.ojo.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

@androidx.room.Database(entities = {Camera.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_FILENAME = "app.db";

    private static AppDatabase instance;

    public abstract CameraDAO cameraDAO();

    public static AppDatabase getInstance(Context context) {
        if (instance == null || ! instance.isOpen()) {
            instance = Room.databaseBuilder(context, AppDatabase.class, DATABASE_FILENAME).allowMainThreadQueries().build();
        }
        return instance;
    }
}
