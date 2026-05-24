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

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.TrashViewHolder> {
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
        int adapterPosition = holder.getBindingAdapterPosition();
        Note note = notes.get(adapterPosition);

        // set text
        holder.tvTitle.setText(note.getTitle());
        holder.tvPreview.setText(note.getContent());
        holder.tvTimestamp.setText(new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                Locale.getDefault()).format(new Date(note.getTimestamp())));

        // long press menu
        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Restore");
            popup.getMenu().add("Delete Forever");

            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();
                if (selected.equals("Restore")) {
                    note.setDeleted(false);
                    note.setDeletedAt(0);
                    NoteDatabase.getInstance(v.getContext()).noteDao().update(note);
                    notes.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                } else if (selected.equals("Delete Forever")) {
                    NoteDatabase.getInstance(v.getContext()).noteDao().delete(note);
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
