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

import com.project.appmusic.MainActivity;
import com.project.appmusic.R;
import com.project.appmusic.viewModel.MusicViewModel;


public class FullPlayerFragment extends Fragment {

    private MusicViewModel musicViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_player, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        ImageView btnFavorite = view.findViewById(R.id.favoriteBtn);
        //datos de la cancion
        TextView txtSongTitle = view.findViewById(R.id.songTitle);
        TextView txtArtistName = view.findViewById(R.id.artistName);
        ImageView imgCover = view.findViewById(R.id.albumCover);

        //control de reproduccion
        ImageView btnPlayPause = view.findViewById(R.id.playBtn);
        ImageView btnShuffle = view.findViewById(R.id.shuffle);
        ImageView btnPrevious = view.findViewById(R.id.skipPrevious);
        ImageView btnNext = view.findViewById(R.id.skipNext);
        ImageView btnRepeat = view.findViewById(R.id.repeat);



    }

    @Override
    public void onStart() {
        super.onStart();
        // ocultar Mini Player
        ((MainActivity) requireActivity()).setMiniPlayerVisibility(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        // restaurar  Mini Player
        ((MainActivity) requireActivity()).setMiniPlayerVisibility(true);
    }
}