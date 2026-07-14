package com.example.securescribe;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);
    @Update
    void update(Note note);
    @Delete
    void delete(Note note);
    @Query("SELECT * FROM notes WHERE is_deleted = 0 AND is_archived = 0 ORDER BY timestamp DESC")
    List<Note> getAllNotes();

    // get archived notes
    @Query("SELECT * FROM notes WHERE is_archived = 1 AND is_deleted = 0 ORDER BY timestamp DESC")
    List<Note> getArchivedNotes();

    // get deleted notes
    @Query("SELECT * FROM notes WHERE is_deleted = 1 ORDER BY timestamp DESC")
    List<Note> getDeletedNotes();

    // permanently delete notes older than 7 days from trash
    @Query("DELETE FROM notes WHERE is_deleted = 1 AND deleted_at < :cutoff")
    void purgeOldDeletedNotes(long cutoff);

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    @Query("SELECT COUNT(*) FROM notes")
    int getTotalCount();

}
