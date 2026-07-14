package com.example.securescribe;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.core.util.Consumer;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import android.database.Cursor;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 2)
public abstract class NoteDatabase extends RoomDatabase {
    public static final String DB_NAME = "note_database";
    public static final MutableLiveData<Exception> dbError = new MutableLiveData<>();

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notes ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN deleted_at INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE notes ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0");
        }
    };
    private static NoteDatabase instance;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();

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
                    DB_NAME
            ).addMigrations(MIGRATION_1_2)
                    .openHelperFactory(factory)
                    .build();
        }
        return instance;
    }

    public static void rekey(Context context, String currentPassword, String newPassword,
                             Runnable onSuccess, Consumer<Exception> onError) {
        databaseWriteExecutor.execute(() -> {
            File currentDbFile = context.getDatabasePath(DB_NAME);
            File tempDbFile = new File(currentDbFile.getPath() + ".rekey_tmp");
            File backupDbFile = new File(currentDbFile.getPath() + ".rekey_backup");

            int liveCount = 0;
            try {
                // Get live count before closing Room
                NoteDatabase db = getInstance(context);
                if (db != null) {
                    liveCount = db.noteDao().getTotalCount();
                }
            } catch (Exception e) {
                // If we can't even get the count, proceed with raw verification only
            }

            // 1. Fully close and nullify Room before touching the file
            synchronized (NoteDatabase.class) {
                if (instance != null) {
                    instance.close();
                    instance = null;
                }
            }

            if (tempDbFile.exists()) tempDbFile.delete();

            SQLiteDatabase.loadLibs(context);
            SQLiteDatabase live = null;
            try {
                // 2. Open RAW SQLCipher connection
                live = SQLiteDatabase.openDatabase(currentDbFile.getAbsolutePath(),
                        currentPassword, null, SQLiteDatabase.OPEN_READWRITE);

                // 3. Export to a brand-new file under the NEW key
                live.execSQL("ATTACH DATABASE '" + tempDbFile.getAbsolutePath() +
                        "' AS rekeyed KEY '" + escape(newPassword) + "'");
                live.execSQL("SELECT sqlcipher_export('rekeyed')");
                live.execSQL("DETACH DATABASE rekeyed");
                live.close();
                live = null;

                // 4. Verification with NEW key
                SQLiteDatabase verifyDb = SQLiteDatabase.openDatabase(
                        tempDbFile.getAbsolutePath(), newPassword, null,
                        SQLiteDatabase.OPEN_READONLY);
                Cursor c = verifyDb.rawQuery("SELECT COUNT(*) FROM notes", null);
                int verifiedCount = 0;
                if (c != null) {
                    if (c.moveToFirst()) {
                        verifiedCount = c.getInt(0);
                    }
                    c.close();
                }
                verifyDb.close();

                if (verifiedCount != liveCount) {
                    throw new IllegalStateException("Row count mismatch after export: "
                            + verifiedCount + " vs " + liveCount);
                }

                // 5. Atomic swap
                if (backupDbFile.exists()) backupDbFile.delete();
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Files.move(currentDbFile.toPath(), backupDbFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                    Files.move(tempDbFile.toPath(), currentDbFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } else {
                    currentDbFile.renameTo(backupDbFile);
                    tempDbFile.renameTo(currentDbFile);
                }

                onSuccess.run();

            } catch (Exception e) {
                if (live != null) live.close();
                if (tempDbFile.exists()) tempDbFile.delete();
                onError.accept(e);
            }
        });
    }

    private static String escape(String s) {
        return s.replace("'", "''");
    }
}
