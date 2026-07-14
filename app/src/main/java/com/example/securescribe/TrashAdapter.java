package com.example.securescribe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {
    private static final String DATE_FORMAT = "dd MMM yyyy, hh:mm a";
    private final List<Note> notes;

    public TrashAdapter(List<Note> notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public TrashViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new TrashViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TrashViewHolder holder, int position) {
        Note note = notes.get(position);

        // set text
        holder.tvTitle.setText(note.getTitle());
        holder.tvPreview.setText(note.getContent());
        holder.tvTimestamp.setText(new SimpleDateFormat(DATE_FORMAT,
                Locale.getDefault()).format(new Date(note.getTimestamp())));

        // long press menu
        holder.itemView.setOnLongClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return true;
            Note currentNote = notes.get(currentPosition);

            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(v.getContext().getString(R.string.menu_restore));
            popup.getMenu().add(v.getContext().getString(R.string.menu_delete_forever));

            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();
                if (selected.equals(v.getContext().getString(R.string.menu_restore))) {
                    currentNote.setDeleted(false);
                    currentNote.setDeletedAt(0);
                    NoteDatabase.databaseWriteExecutor.execute(() -> {
                        NoteDatabase.getInstance(v.getContext()).noteDao().update(currentNote);
                        ((TrashActivity) v.getContext()).runOnUiThread(() -> {
                            notes.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                        });
                    });
                } else if (selected.equals(v.getContext().getString(R.string.menu_delete_forever))) {
                    NoteDatabase.databaseWriteExecutor.execute(() -> {
                        NoteDatabase.getInstance(v.getContext()).noteDao().delete(currentNote);
                        ((TrashActivity) v.getContext()).runOnUiThread(() -> {
                            notes.remove(currentPosition);
                            notifyItemRemoved(currentPosition);
                        });
                    });
                }
                return true;
            });
            popup.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class TrashViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview, tvTimestamp;
        public TrashViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.viewTitle);
            tvPreview = itemView.findViewById(R.id.viewPreview);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
