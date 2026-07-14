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

    private static final String DATE_FORMAT = "dd MMM yyyy, hh:mm a";
    private final List<Note> notes;
    private final NoteActionListener listener;

    public NoteAdapter(List<Note> notes, NoteActionListener listener) {
        this.notes = notes;
        this.listener = listener;
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
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvPreview.setText(note.getContent());

        String formatted = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                .format(new Date(note.getTimestamp()));
        holder.tvTimestamp.setText(formatted);
        holder.itemView.setOnLongClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return true;
            Note currentNote = notes.get(currentPosition);

            // show popup menu
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(v.getContext().getString(R.string.menu_edit));
            popup.getMenu().add(v.getContext().getString(R.string.menu_delete));
            popup.getMenu().add(v.getContext().getString(R.string.menu_archive));
            popup.getMenu().add(v.getContext().getString(R.string.menu_share));
            popup.getMenu().add(v.getContext().getString(R.string.menu_copy_text));

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String selected = item.getTitle().toString();
                    if (selected.equals(v.getContext().getString(R.string.menu_delete))) {
                        if (listener != null) {
                            listener.onNoteDeleted(currentNote, currentPosition);
                        }
                    } else if (selected.equals(v.getContext().getString(R.string.menu_edit))) {
                        Intent intent = new Intent(v.getContext(), AddEditNoteActivity.class);
                        intent.putExtra("note_id", currentNote.getId());
                        intent.putExtra("note_title", currentNote.getTitle());
                        intent.putExtra("note_content", currentNote.getContent());
                        v.getContext().startActivity(intent);
                    } else if (selected.equals(v.getContext().getString(R.string.menu_share))) {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, currentNote.getTitle() + "\n\n" + currentNote.getContent());
                        v.getContext().startActivity(Intent.createChooser(shareIntent, v.getContext().getString(R.string.msg_share_note)));
                    } else if (selected.equals(v.getContext().getString(R.string.menu_copy_text))) {
                        ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("note", currentNote.getTitle() + "\n\n" + currentNote.getContent());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(v.getContext(), R.string.msg_copied, Toast.LENGTH_SHORT).show();
                    } else if (selected.equals(v.getContext().getString(R.string.menu_archive))) {
                        if (listener != null) {
                            listener.onNoteArchived(currentNote, currentPosition);
                        }
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

    public void removeItem(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setNotes(List<Note> newNotes) {
        this.notes.clear();
        this.notes.addAll(newNotes);
        notifyDataSetChanged();
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