package com.project.appmusic.playbackFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.project.appmusic.Playlist;
import com.project.appmusic.R;
import com.project.appmusic.Song;
import com.project.appmusic.data.entity.PlaylistEntity;
import com.project.appmusic.viewModel.MusicViewModel;


public class MiniPlayerFragment extends Fragment {

    private MusicViewModel musicViewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mini_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        // esta vista se oculta por defecto
        view.setVisibility(View.GONE);

        ImageView btnPlayPause = view.findViewById(R.id.playBtn);
        ImageView btnFavorite = view.findViewById(R.id.favoriteBtn);
        TextView txtSongTitle = view.findViewById(R.id.songTitle);
        TextView txtArtistName = view.findViewById(R.id.artistName);
        ImageView imgCover = view.findViewById(R.id.coverImage);

        // observador de estado de reproducción
        musicViewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            if (isPlaying != null && isPlaying) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
            }
        });

        // observador de datos de la canción
        musicViewModel.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            if (song != null) {
                //  cambio de estado, se muestra el reproductor en pantalla
                view.setVisibility(View.VISIBLE);
                txtSongTitle.setText(song.getTitulo());
                txtArtistName.setText(song.getNameArtist());
                Glide.with(this).load(song.getUrlPortada()).transform(new RoundedCorners(16)).into(imgCover);
            }
        });

        btnPlayPause.setOnClickListener(v -> {
            musicViewModel.togglePlayback();
        });


        view.setOnClickListener(v -> {
            // apuntamos al contenedor de pantalla completa
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_fullscreen_container, new FullPlayerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        musicViewModel.getIsCurrentFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
            if (isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite);
            }
        });

        btnFavorite.setOnClickListener(v -> {
            Song currentSong = musicViewModel.getCurrentSong().getValue();
            if (currentSong != null) {
                musicViewModel.toggleFavorite(currentSong);
            }
        });



    }

}