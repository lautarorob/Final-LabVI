package com.project.appmusic.reciclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.appmusic.MainActivity;
import com.project.appmusic.R;
import com.project.appmusic.Song;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ListItemHolder> {

    private List<Song> songs;

    private MainActivity mainActivity;

    public SongAdapter(MainActivity mainActivity, List<Song> songs) {
        this.mainActivity = mainActivity;
        this.songs = songs;
    }

    //inflado del item_song y creacion del holder
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
        Glide.with(mainActivity)
                .load(song.getUrlPortada())
                .into(holder.coverImage);


    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ListItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView songTitle;
        TextView artistName;
        ImageView coverImage;
        //  ImageView optionsBtn;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);

            songTitle = itemView.findViewById(R.id.songTitle);
            artistName = itemView.findViewById(R.id.artistName);
            coverImage = itemView.findViewById(R.id.coverImage);
            //  optionsBtn = itemView.findViewById(R.id.optionsBtn);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(mainActivity, songs.get(getAdapterPosition()).getTitulo(), Toast.LENGTH_SHORT).show();
        }
    }
}