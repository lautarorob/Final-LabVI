package com.project.appmusic.reciclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.appmusic.R;
import com.project.appmusic.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ListItemHolder> {

    private List<Song> songs;
    private Context context;
    private OnSongClickListener listener;

    private boolean isTopChart;

    public SongAdapter(Context context, List<Song> songs, boolean isTopChart, OnSongClickListener listener) {
        this.context = context;
        this.songs = songs;
        this.isTopChart = isTopChart;
        this.listener = listener;
    }

    @Override
    public ListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemHolder holder, int position) {
        Song song = songs.get(position);

        holder.songTitle.setText(song.getTitulo());
        holder.artistName.setText(song.getNameArtist());
        Glide.with(context)
                .load(song.getUrlPortada())
                .into(holder.coverImage);

        if (isTopChart) {
            holder.txtRanking.setVisibility(View.VISIBLE);

            holder.txtRanking.setText(String.valueOf(position + 1));
        } else {
            // si es búsqueda normal se oculta el número
            holder.txtRanking.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ListItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView songTitle;
        TextView artistName;
        ImageView coverImage;

        TextView txtRanking;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);

            songTitle = itemView.findViewById(R.id.songTitle);
            artistName = itemView.findViewById(R.id.artistName);
            coverImage = itemView.findViewById(R.id.coverImage);


            txtRanking = itemView.findViewById(R.id.txt_ranking_number);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                listener.onSongClick(songs.get(position));
            }
        }
    }

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }
}