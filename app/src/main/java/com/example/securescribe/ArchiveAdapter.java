package com.example.securescribe;

import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ArchiveViewHolder> {

    private final List<Note> notes;
    public ArchiveAdapter(List<Note> notes) {
        this.notes = notes;
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
        int adapterPosition = holder.getBindingAdapterPosition();
        Note note = notes.get(adapterPosition);

        holder.tvTitle.setText(note.getTitle());
        holder.tvPreview.setText(note.getContent());
        holder.tvTimestamp.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                Locale.getDefault()).format(new Date(note.getTimestamp())));
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Unarchive");

            popup.setOnMenuItemClickListener(item -> {
                String selected = Objects.requireNonNull(item.getTitle()).toString();
                if (selected.equals("Unarchive")) {
                    note.setArchived(false);
                    NoteDatabase.getInstance(v.getContext()).noteDao().update(note);
                    notes.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
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
