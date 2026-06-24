package com.project.appmusic.reciclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.appmusic.objetos.Playlist;
import com.project.appmusic.R;
import com.project.appmusic.data.entity.PlaylistEntity;
import com.project.appmusic.data.entity.PlaylistWithTracks;
import com.project.appmusic.data.entity.TrackEntity;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private List<PlaylistWithTracks> playlists = new ArrayList<>();
    private OnPlaylistClickListener listener;

    private OnPlaylistLongClickListener longClickListener;

    private final boolean showAddIcon;


    public PlaylistAdapter(boolean showAddIcon, OnPlaylistClickListener listener, OnPlaylistLongClickListener longClickListener) {
        this.showAddIcon = showAddIcon;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    public PlaylistAdapter(Context context, List<PlaylistEntity> playlists, boolean showAddIcon) {
        this.showAddIcon = showAddIcon;
    }

    public void setPlaylists(List<PlaylistWithTracks> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistWithTracks item = playlists.get(position);

        holder.tvPlaylistName.setText(item.playlist.name);

        if (this.showAddIcon) {
            holder.ivStatusIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivStatusIcon.setVisibility(View.GONE);
        }

        if (item.playlist.isFavorites) {
            holder.ivPlaylistCover.setImageResource(R.drawable.like_logo);
        } else {
            if (item.tracks != null && !item.tracks.isEmpty()) {
                TrackEntity ultimaCancion = item.tracks.get(item.tracks.size() - 1);
                String urlPortada = ultimaCancion.coverUrl;

                Glide.with(holder.itemView.getContext())
                        .load(urlPortada)
                        .placeholder(R.color.black) // Imagen de carga temporal
                        .into(holder.ivPlaylistCover);
            } else {
                holder.ivPlaylistCover.setImageResource(R.drawable.ic_add_circle);
                holder.ivPlaylistCover.setBackgroundColor(Color.parseColor("#2A2A2A"));
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlaylistClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onPlaylistLongClick(item);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void setSimplePlaylists(List<Playlist> playlistsFiltradas) {
        this.playlists.clear();

        if (playlistsFiltradas != null) {
            for (Playlist playlistSimple : playlistsFiltradas) {
                PlaylistWithTracks playlistCompleta = new PlaylistWithTracks();

                PlaylistEntity entity = new PlaylistEntity();
                entity.playlistId = (int) playlistSimple.getId();
                entity.name = playlistSimple.getName();

                playlistCompleta.playlist = entity;

                playlistCompleta.tracks = new ArrayList<>();

                this.playlists.add(playlistCompleta);
            }
        }
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaylistName;
        com.google.android.material.imageview.ShapeableImageView ivPlaylistCover;

        ImageView ivStatusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            ivPlaylistCover = itemView.findViewById(R.id.ivPlaylistCover);
            ivStatusIcon = itemView.findViewById(R.id.ivStatusIcon);
        }
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(PlaylistWithTracks playlistSeleccionada);
    }

    public interface OnPlaylistLongClickListener {
        void onPlaylistLongClick(PlaylistWithTracks playlistSeleccionada);
    }
}
