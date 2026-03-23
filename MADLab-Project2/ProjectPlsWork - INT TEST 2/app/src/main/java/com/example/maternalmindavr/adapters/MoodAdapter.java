package com.example.maternalmindavr.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.maternalmindavr.R;
import com.example.maternalmindavr.models.MoodEntry;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
    private List<MoodEntry> moodList;

    public MoodAdapter(List<MoodEntry> moodList) {
        this.moodList = moodList;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_log, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodEntry entry = moodList.get(position);
        holder.tvMood.setText(entry.getMood());
        holder.tvNote.setText(entry.getNote());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(entry.getTimestamp()));
        holder.tvDate.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return moodList.size();
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvMood, tvNote, tvDate;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvItemMood);
            tvNote = itemView.findViewById(R.id.tvItemNote);
            tvDate = itemView.findViewById(R.id.tvItemDate);
        }
    }
}
