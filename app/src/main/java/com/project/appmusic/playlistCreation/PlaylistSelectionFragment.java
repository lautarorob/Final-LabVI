package com.project.appmusic.playlistCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.reciclerView.PlaylistAdapter;
import com.project.appmusic.viewModel.MusicViewModel;


public class PlaylistSelectionFragment extends BottomSheetDialogFragment {

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;


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

        PlaylistAdapter adapter = new PlaylistAdapter(
                true,

                //  Click normal
                playlistSelect -> {
                    musicViewModel.addSongToExistingPlaylist(playlistSelect.playlist.playlistId, songSelect);
                    dismiss();
                },

                // Click largo
                null
        );

        // Vincular el adaptador a la vista
        recyclerPlaylists.setAdapter(adapter);

        //  Observar la base de datos y pintar las filas
        musicViewModel.getUserPlaylistsLiveData().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                adapter.setPlaylists(playlists);
            }
        });

        musicViewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                musicViewModel.toastMessageLiveData.setValue(null);
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

        musicViewModel.getSearchPlaylistLiveData().observe(getViewLifecycleOwner(), playlistsFiltradas -> {
            if (playlistsFiltradas != null) {
                adapter.setPlaylists(playlistsFiltradas);
            }
        });

        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    musicViewModel.searchPlaylist(query);
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                if (newText != null && !newText.trim().isEmpty()) {
                    searchRunnable = () -> musicViewModel.searchPlaylist(newText);
                    handler.postDelayed(searchRunnable, 500);
                } else {
                    musicViewModel.loadUserPlaylists();
                }
                return true;
            }
        });


        // Disparar la consulta a Room
        musicViewModel.loadUserPlaylists();
    }
}