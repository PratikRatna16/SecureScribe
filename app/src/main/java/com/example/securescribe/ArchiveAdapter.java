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
import java.util.Objects;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ArchiveViewHolder> {

    private static final String DATE_FORMAT = "dd MMM yyyy, hh:mm a";
    private final List<Note> notes;
    private final NoteActionListener listener;

    public ArchiveAdapter(List<Note> notes, NoteActionListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArchiveViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new ArchiveViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder( @NonNull ArchiveViewHolder  holder, int position) {
        Note note = notes.get(position);

        holder.tvTitle.setText(note.getTitle());
        holder.tvPreview.setText(note.getContent());
        holder.tvTimestamp.setText(new SimpleDateFormat(DATE_FORMAT,
                Locale.getDefault()).format(new Date(note.getTimestamp())));
        holder.itemView.setOnLongClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return true;
            Note currentNote = notes.get(currentPosition);

            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(v.getContext().getString(R.string.menu_unarchive));

            popup.setOnMenuItemClickListener(item -> {
                String selected = Objects.requireNonNull(item.getTitle()).toString();
                if (selected.equals(v.getContext().getString(R.string.menu_unarchive))) {
                    if (listener != null) {
                        listener.onNoteUnarchived(currentNote, currentPosition);
                    }
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

    public void removeItem(int position) {
        if (position >= 0 && position < notes.size()) {
            notes.remove(position);
            notifyItemRemoved(position);
        }
    }

    static class ArchiveViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview, tvTimestamp;
        public ArchiveViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.viewTitle);
            tvPreview = itemView.findViewById(R.id.viewPreview);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
