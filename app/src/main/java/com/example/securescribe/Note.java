package com.example.securescribe;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
        @PrimaryKey(autoGenerate = true)
        int id;
        @ColumnInfo(name = "title")
    String title;
        @ColumnInfo(name = "content")
    String content;
        @ColumnInfo(name = "timestamp")
    long timestamp;
    @ColumnInfo(name = "is_deleted")
    private boolean isDeleted;

    @ColumnInfo(name = "deleted_at")
    private long deletedAt;

    @ColumnInfo(name = "is_archived")
    private boolean isArchived;
        public Note(String title, String content, long timestamp){
            this.title = title;
            this.content = content;
            this.timestamp = timestamp;
            this.isDeleted = false;
            this.deletedAt = 0;
            this.isArchived = false;
        }
        public int getId(){ return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle(){ return title; }
    public String getContent(){ return content; }
    public long getTimestamp(){ return timestamp; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { isArchived = archived; }
}
