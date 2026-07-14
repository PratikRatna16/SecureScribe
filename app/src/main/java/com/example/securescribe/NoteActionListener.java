package com.example.securescribe;

public interface NoteActionListener {
    void onNoteDeleted(Note note, int position);
    void onNoteArchived(Note note, int position);
    void onNoteUnarchived(Note note, int position);
    void onNoteRestored(Note note, int position);
    void onNotePermanentlyDeleted(Note note, int position);
}
