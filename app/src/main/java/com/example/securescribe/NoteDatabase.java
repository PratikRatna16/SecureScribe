package com.example.securescribe;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 2)
public abstract class NoteDatabase extends RoomDatabase {
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN deleted_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0");
        }
    };
    private static NoteDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract NoteDao noteDao();

    public static synchronized NoteDatabase getInstance(Context context){
        if(instance == null){
            SharedPreferences prefs = SecurityUtils.getEncryptedSharedPreferences(context);
            String password = prefs.getString("password", null);

            if (password == null) {
                // Not initialized yet
                return null;
            }

            // Load SQLCipher libraries
            SQLiteDatabase.loadLibs(context);

            byte[] passphrase = SQLiteDatabase.getBytes(password.toCharArray());
            SupportFactory factory = new SupportFactory(passphrase);

            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    NoteDatabase.class,
                    "note_database"
            ).addMigrations(MIGRATION_1_2)
                    .openHelperFactory(factory)
                    .build();
        }
        return instance;
    }
}
