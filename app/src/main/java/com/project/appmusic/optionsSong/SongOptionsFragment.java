package com.project.appmusic.optionsSong;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.appmusic.R;
import com.project.appmusic.Song;

import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.appmusic.playlistCreation.PlaylistSelectionFragment;


public class SongOptionsFragment extends BottomSheetDialogFragment {

    private Song songSeleccionada;

    public SongOptionsFragment() {
    }

    public void setSong(Song song) {
        this.songSeleccionada = song;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (songSeleccionada == null) {
            dismiss();
            return;
        }

        TextView btnAddToPlaylist = view.findViewById(R.id.btnAddToPlaylist);
        TextView btnAddToQueue = view.findViewById(R.id.btnAddToRow);

        btnAddToPlaylist.setOnClickListener(v -> {
            PlaylistSelectionFragment playlistSelectionFragment = new PlaylistSelectionFragment();
            playlistSelectionFragment.setSong(this.songSeleccionada);
            playlistSelectionFragment.show(getParentFragmentManager(), "playlistSelection");
            dismiss();
        });

        btnAddToQueue.setOnClickListener(v -> {
            dismiss();
        });
    }
}