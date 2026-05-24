package com.example.securescribe;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final List<Note> notes;

    public NoteAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        int adapterPosition = holder.getBindingAdapterPosition();
        Note note = notes.get(adapterPosition);
        holder.tvTitle.setText(note.getTitle());
        holder.tvPreview.setText(note.getContent());

        String formatted = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(note.getTimestamp()));
        holder.tvTimestamp.setText(formatted);
        holder.itemView.setOnLongClickListener(v -> {
            // show popup menu
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");
            popup.getMenu().add("Archive");
            popup.getMenu().add("Share");
            popup.getMenu().add("Copy Text");

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String selected = item.getTitle().toString();
                    if (selected.equals("Delete")) {
                        note.setDeleted(true);
                        note.setDeletedAt(System.currentTimeMillis());
                        NoteDatabase.getInstance(v.getContext()).noteDao().update(note);
                        notes.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    } else if (selected.equals("Edit")) {
                        Intent intent = new Intent(v.getContext(), AddEditNoteActivity.class);
                        intent.putExtra("note_id", note.getId());
                        intent.putExtra("note_title", note.getTitle());
                        intent.putExtra("note_content", note.getContent());
                        v.getContext().startActivity(intent);
                    } else if (selected.equals("Share")) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + "\n\n" + note.getContent());
                        v.getContext().startActivity(Intent.createChooser(shareIntent, "Share note"));
                    } else if (selected.equals("Copy Text")) {
                        ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("note", note.getTitle() + "\n\n" + note.getContent());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(v.getContext(), "Copied", Toast.LENGTH_SHORT).show();
                    } else if (selected.equals("Archive")) {
                    note.setArchived(true);
                    NoteDatabase.getInstance(v.getContext()).noteDao().update(note);
                    notes.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                }
                    return true;
                }
            });
            popup.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvPreview, tvTimestamp;

        public NoteViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.viewTitle);
            tvPreview = itemView.findViewById(R.id.viewPreview);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}