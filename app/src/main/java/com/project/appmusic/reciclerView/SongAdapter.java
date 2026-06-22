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

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ListItemHolder> {

    private List<Song> songs = new ArrayList<>();
    private OnSongClickListener listener;
    private boolean isTopChart;
    private List<Long> favoriteIds = new ArrayList<>();

    private boolean showFavoriteIcon;

    // Constructor 1: Original (Completo)
    public SongAdapter(Context context, List<Song> songs, boolean isTopChart, boolean showFavoriteIcon, OnSongClickListener listener) {
        this.songs = songs != null ? songs : new ArrayList<>();
        this.isTopChart = isTopChart;
        this.showFavoriteIcon = showFavoriteIcon;
        this.listener = listener;
    }

    // Constructor 2: Sobrecarga para inicialización vacía (Usado en PlayListFragment)
    public SongAdapter(boolean showFavoriteIcon,OnSongClickListener listener) {
        this.showFavoriteIcon = showFavoriteIcon;
        this.listener = listener;
        this.isTopChart = false;
    }

    @NonNull
    @Override
    public ListItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ListItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemHolder holder, int position) {
        Song song = songs.get(position);

        // Asignación de texto
        holder.songTitle.setText(song.getTitulo());
        holder.artistName.setText(song.getNameArtist());

        // Inyección de imagen con contexto extraído dinámicamente de la vista
        Glide.with(holder.itemView.getContext())
                .load(song.getUrlPortada())
                .into(holder.coverImage);

        // Lógica de ranking
        if (isTopChart) {
            holder.txtRanking.setVisibility(View.VISIBLE);
            holder.txtRanking.setText(String.valueOf(position + 1));
        } else {
            holder.txtRanking.setVisibility(View.GONE);
        }

        if (this.showFavoriteIcon) {
            holder.btnFavoriteItem.setVisibility(View.VISIBLE);

            // Lógica de llenado de corazón existente
            if (favoriteIds != null && favoriteIds.contains(song.getId())) {
                holder.btnFavoriteItem.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                holder.btnFavoriteItem.setImageResource(R.drawable.ic_favorite);
            }
        } else {
            holder.btnFavoriteItem.setVisibility(View.GONE);
        }

        // Lógica de UI para Favoritos
        if (favoriteIds != null && favoriteIds.contains(song.getId())) {
            holder.btnFavoriteItem.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.btnFavoriteItem.setImageResource(R.drawable.ic_favorite);
        }

        // Delegación de eventos táctiles
        holder.btnFavoriteItem.setOnClickListener(v -> {
            boolean isFav = favoriteIds != null && favoriteIds.contains(song.getId());
            if (isFav) {
                holder.btnFavoriteItem.setImageResource(R.drawable.ic_favorite);
            } else {
                holder.btnFavoriteItem.setImageResource(R.drawable.ic_favorite_filled);
            }
            if (listener != null) {
                listener.onFavoriteClick(song);
            }
        });

        holder.optionsBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionsClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    public void setFavoriteIds(List<Long> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    public class ListItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView songTitle;
        TextView artistName;
        ImageView coverImage;
        TextView txtRanking;
        ImageView btnFavoriteItem;
        ImageView optionsBtn;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.songTitle);
            artistName = itemView.findViewById(R.id.artistName);
            coverImage = itemView.findViewById(R.id.coverImage);
            optionsBtn = itemView.findViewById(R.id.optionsBtn);
            txtRanking = itemView.findViewById(R.id.txt_ranking_number);
            btnFavoriteItem = itemView.findViewById(R.id.btnFavoriteItem);

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

    // Contrato de interfaz actualizado
    public interface OnSongClickListener {
        void onSongClick(Song song);
        void onFavoriteClick(Song song);
        void onOptionsClick(Song song);

        // Firma requerida para la lógica de borrado desde una playlist
        void onRemoveFromPlaylistClick(Song song);
    }
}