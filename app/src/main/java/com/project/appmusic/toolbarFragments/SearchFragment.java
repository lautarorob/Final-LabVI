package com.project.appmusic.toolbarFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private MusicViewModel musicViewModel;
    private SongAdapter songAdapter;

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vinculación de vistas
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerSongs = view.findViewById(R.id.recyclersongs);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        songAdapter = new SongAdapter(requireContext(), new ArrayList<>(), false, true, new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                musicViewModel.playSong(song, null);
            }

            @Override
            public void onFavoriteClick(Song song) {
                musicViewModel.toggleFavorite(song);
            }

            @Override
            public void onOptionsClick(Song song) {
                SongOptionsFragment songOptionsFragment = new SongOptionsFragment();
                songOptionsFragment.setSong(song);
                songOptionsFragment.show(getParentFragmentManager(), "songOptions");
            }

            @Override
            public void onRemoveFromPlaylistClick(Song song) {
                // No es necesario en este caso
            }
        });

        // Vinculamos el adaptador a la vista
        recyclerSongs.setAdapter(songAdapter);

        // Observador directo de resultados de búsqueda para actualizar el adaptador existente
        musicViewModel.getSearchSongsLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && songAdapter != null) {
                songAdapter.setSongs(songs);
            }
        });
/*
        // Modificación del Texto y Hint
        android.widget.EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(android.graphics.Color.BLACK);
            searchEditText.setHintTextColor(android.graphics.Color.BLACK);
        }

        // Modificación del ícono de la lupa
        android.widget.ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            searchIcon.setColorFilter(android.graphics.Color.BLACK);
        }*/

        // Configuración de la barra de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    musicViewModel.downloadSongs(query);
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
                    searchRunnable = () -> musicViewModel.downloadSongs(newText);
                    handler.postDelayed(searchRunnable, 500);
                } else {
                    if (songAdapter != null) {
                        songAdapter.setSongs(new ArrayList<>());
                    }
                }
                return true;
            }
        });

        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (favoriteIds != null && songAdapter != null) {
                songAdapter.setFavoriteIds(favoriteIds);
            }
        });

        musicViewModel.loadFavoriteIds();
    }
}