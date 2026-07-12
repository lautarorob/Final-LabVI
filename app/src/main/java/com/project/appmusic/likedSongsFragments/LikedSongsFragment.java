package com.project.appmusic.likedSongsFragments;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

import java.util.ArrayList;
import java.util.List;

public class LikedSongsFragment extends Fragment {

    private MusicViewModel musicViewModel;
    private RecyclerView recyclerFavorites;
    private TextView tvSongCount;
    private TextView tvEmptyFavorites;
    private HorizontalScrollView scrollFilters;
    private ChipGroup chipGroupFilters;
    private SearchView searchView;
    private SongAdapter adapter;
    private List<Song> originalFavoriteSongs = new ArrayList<>();
    private String currentSearchQuery = "";
    private List<String> currentSelectedGenres = new ArrayList<>();

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    public LikedSongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Enlace de vistas
        recyclerFavorites = view.findViewById(R.id.recyclersongs);
        tvSongCount = view.findViewById(R.id.SongCount);
        tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites);
        scrollFilters = view.findViewById(R.id.scrollFilters);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        searchView = view.findViewById(R.id.search_view);
        ImageView btnBack = view.findViewById(R.id.btnBack);

        recyclerFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));
        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        // Inicialización del Adaptador
        adapter = new SongAdapter(requireContext(), new ArrayList<>(), false, true, new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                musicViewModel.playSong(song, originalFavoriteSongs);
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
            public void onRemoveFromPlaylistClick(Song song) { }
        });
        recyclerFavorites.setAdapter(adapter);

        // --- OBSERVADOR PRINCIPAL DE LA BASE DE DATOS ---
        musicViewModel.getFavoritesLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty()) {
                originalFavoriteSongs = songs; // Guardamos la lista maestra

                // Hacemos visible la UI
                scrollFilters.setVisibility(View.VISIBLE);
                tvSongCount.setVisibility(View.VISIBLE);
                recyclerFavorites.setVisibility(View.VISIBLE);
                tvEmptyFavorites.setVisibility(View.GONE);

                // Disparamos el filtro por si quedó alguna búsqueda o chip activo
                aplicarFiltros();
            } else {
                originalFavoriteSongs.clear();

                // Ocultamos la UI
                scrollFilters.setVisibility(View.GONE);
                tvSongCount.setVisibility(View.GONE);
                recyclerFavorites.setVisibility(View.GONE);
                tvEmptyFavorites.setVisibility(View.VISIBLE);

                // Limpiamos filtros
                chipGroupFilters.clearCheck();
                currentSearchQuery = "";
                searchView.setQuery("", false);

                adapter.setSongs(new ArrayList<>());
            }
        });

        // Observador de los iconos de corazones
        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (adapter != null) {
                adapter.setFavoriteIds(favoriteIds);
            }
        });

        // --- LISTENER DEL BUSCADOR ---
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> aplicarFiltros();
                handler.postDelayed(searchRunnable, 300);
                return true;
            }
        });

        // --- LISTENER DE LOS CHIPS DE GÉNERO ---
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentSelectedGenres.clear();
            for (int id : checkedIds) {
                Chip chip = view.findViewById(id);
                if (chip != null) {
                    currentSelectedGenres.add(chip.getText().toString().toLowerCase());
                }
            }
            aplicarFiltros();
        });

        // Carga inicial
        musicViewModel.loadFavorites();
        musicViewModel.loadFavoriteIds();
    }

    private void aplicarFiltros() {
        List<Song> filteredList = new ArrayList<>();
        String queryLower = currentSearchQuery.toLowerCase();

        for (Song song : originalFavoriteSongs) {

            // Filtro de Texto
            boolean matchesText = queryLower.isEmpty() ||
                    (song.getTitulo() != null && song.getTitulo().toLowerCase().contains(queryLower)) ||
                    (song.getNameArtist() != null && song.getNameArtist().toLowerCase().contains(queryLower));

            // Filtro de Género
            boolean matchesGenre = currentSelectedGenres.isEmpty();

            // Validamos usando el nuevo getter en plural
            if (!matchesGenre && song.getGenres() != null && !song.getGenres().isEmpty()) {
                // Iteramos la lista de géneros de la canción
                for (String songGenre : song.getGenres()) {
                    String songGenreLower = songGenre.toLowerCase();

                    // Comparamos contra la lista de chips seleccionados
                    for (String selectedGenre : currentSelectedGenres) {
                        if (songGenreLower.contains(selectedGenre)) {
                            matchesGenre = true;
                            break; // Rompe el for de chips (ya encontramos coincidencia)
                        }
                    }
                    if (matchesGenre) break; // Rompe el for de géneros (ya sabemos que esta canción sirve)
                }
            }

            // Intersección
            if (matchesText && matchesGenre) {
                filteredList.add(song);
            }
        }

        // Actualizacion del adaptador con la lista filtrada
        adapter.setSongs(filteredList);

        // Actualizamos el contador dinámico de canciones
        int cantidad = filteredList.size();
        if (cantidad == 1) {
            tvSongCount.setText(getString(R.string.song_count_single));
        } else {
            tvSongCount.setText(getString(R.string.song_count_multiple, cantidad));
        }
    }
}