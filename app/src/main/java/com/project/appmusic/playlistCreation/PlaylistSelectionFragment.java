package com.project.appmusic.playlistCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.appmusic.R;
import com.project.appmusic.Song;
import com.project.appmusic.data.entity.PlaylistEntity;
import com.project.appmusic.data.entity.PlaylistWithTracks;
import com.project.appmusic.reciclerView.PlaylistAdapter;
import com.project.appmusic.viewModel.MusicViewModel;


public class PlaylistSelectionFragment extends BottomSheetDialogFragment {


    public PlaylistSelectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_selection, container, false);
    }

    MusicViewModel musicViewModel;
    private Song songSelect;

    public void setSong(Song song) {
        this.songSelect = song;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        RecyclerView recyclerPlaylists = view.findViewById(R.id.recyclerPlaylists);
        recyclerPlaylists.setLayoutManager(new LinearLayoutManager(requireContext()));
        LinearLayout btnCreatePlaylist = view.findViewById(R.id.btnCreateNewPlaylist);

        PlaylistAdapter adapter = new PlaylistAdapter(true, new PlaylistAdapter.OnPlaylistClickListener() {
            @Override
            public void onPlaylistClick(PlaylistWithTracks playlistSelect) {
                musicViewModel.addSongToExistingPlaylist(playlistSelect.playlist.playlistId, songSelect);
                dismiss();
            }
        });

        // Vincular el adaptador a la vista
        recyclerPlaylists.setAdapter(adapter);

        //  Observar la base de datos y pintar las filas
        musicViewModel.getUserPlaylistsLiveData().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                adapter.setPlaylists(playlists);
            }
        });

        btnCreatePlaylist.setOnClickListener(v -> {
            CreatePlaylistFragment createPlaylistFragment = new CreatePlaylistFragment();

            createPlaylistFragment.setSong(this.songSelect);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fullscreen_container, createPlaylistFragment)
                    .addToBackStack(null)
                    .commit();
            dismiss();
        });

        // Disparar la consulta a Room
        musicViewModel.loadUserPlaylists();
    }
}