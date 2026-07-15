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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends Fragment {

    private MusicViewModel musicViewModel;
    private int playlistId;

    private RecyclerView recyclerPlaylistSongs;
    private TextView txtPlaylistTitle;
    private TextView txtSongCount;
    private TextView tvEmptyPlaylist;
    private androidx.appcompat.widget.SearchView searchView;
    private HorizontalScrollView scrollFilters;
    private ChipGroup chipGroupFilters;
    private SongAdapter songAdapter;
    private List<Song> originalPlaylistSongs = new ArrayList<>();
    private String currentSearchQuery = "";
    private List<String> currentSelectedGenres = new ArrayList<>();

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    public PlayListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_list, container, false);
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        txtPlaylistTitle = view.findViewById(R.id.txtPlaylistTitle);
        txtSongCount = view.findViewById(R.id.txtSongCount);
        tvEmptyPlaylist = view.findViewById(R.id.tvEmptyPlaylist);
        searchView = view.findViewById(R.id.search_view);
        recyclerPlaylistSongs = view.findViewById(R.id.recyclerPlaylistSongs);
        scrollFilters = view.findViewById(R.id.scrollFilters);
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        recyclerPlaylistSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        songAdapter = new SongAdapter(requireContext(), new ArrayList<>(), false, true, new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                musicViewModel.playSong(song, originalPlaylistSongs);
            }

            @Override
            public void onFavoriteClick(Song song) {
                musicViewModel.toggleFavorite(song);
            }

            @Override
            public void onOptionsClick(Song song) {
                SongOptionsFragment songOptionsFragment = new SongOptionsFragment();
                songOptionsFragment.setSong(song);
                songOptionsFragment.setOriginPlaylistId(playlistId);
                songOptionsFragment.show(getParentFragmentManager(), "songOptions");
            }

            @Override
            public void onRemoveFromPlaylistClick(Song song) {
                musicViewModel.removeSongFromPlaylist(playlistId, song.getId());
            }
        });

        recyclerPlaylistSongs.setAdapter(songAdapter);

        musicViewModel.getCurrentPlaylistInfoLiveData().observe(getViewLifecycleOwner(), playlistInfo -> {
            if (playlistInfo != null) {
                txtPlaylistTitle.setText(playlistInfo.name);
            }
        });

        musicViewModel.getSongsPlaylistLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty()) {
                originalPlaylistSongs = new ArrayList<>(songs);

                scrollFilters.setVisibility(View.VISIBLE);
                tvEmptyPlaylist.setVisibility(View.GONE);
                recyclerPlaylistSongs.setVisibility(View.VISIBLE);

                aplicarFiltros();
            } else {
                originalPlaylistSongs.clear();

                scrollFilters.setVisibility(View.GONE);
                tvEmptyPlaylist.setVisibility(View.VISIBLE);
                recyclerPlaylistSongs.setVisibility(View.GONE);

                chipGroupFilters.clearCheck();
                currentSearchQuery = "";
                searchView.setQuery("", false);

                songAdapter.setSongs(new ArrayList<>());
                txtSongCount.setText(getResources().getQuantityString(R.plurals.song_count, 0, 0));
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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

        musicViewModel.loadPlaylist(this.playlistId);

        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (songAdapter != null) {
                songAdapter.setFavoriteIds(favoriteIds);
            }
        });

        musicViewModel.loadFavoriteIds();
    }

    private void aplicarFiltros() {
        List<Song> filteredList = new ArrayList<>();
        String queryLower = currentSearchQuery.toLowerCase();

        for (Song song : originalPlaylistSongs) {

            boolean matchesText = queryLower.isEmpty() ||
                    (song.getTitulo() != null && song.getTitulo().toLowerCase().contains(queryLower)) ||
                    (song.getNameArtist() != null && song.getNameArtist().toLowerCase().contains(queryLower));

            boolean matchesGenre = currentSelectedGenres.isEmpty();

            if (!matchesGenre && song.getGenres() != null && !song.getGenres().isEmpty()) {
                for (String songGenre : song.getGenres()) {
                    String songGenreLower = songGenre.toLowerCase();

                    for (String selectedGenre : currentSelectedGenres) {
                        if (songGenreLower.contains(selectedGenre)) {
                            matchesGenre = true;
                            break;
                        }
                    }
                    if (matchesGenre) break;
                }
            }

            if (matchesText && matchesGenre) {
                filteredList.add(song);
            }
        }

        songAdapter.setSongs(filteredList);

        int amount = filteredList.size();
        String textoContador = getResources().getQuantityString(R.plurals.song_count, amount, amount);
        txtSongCount.setText(textoContador);
    }
}