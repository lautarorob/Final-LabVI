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
import android.widget.TextView;

import com.project.appmusic.R;
import com.project.appmusic.Song;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

public class PlayListFragment extends Fragment {

    private MusicViewModel musicViewModel;
    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;
    private int playlistId;

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

        TextView txtPlaylistTitle = view.findViewById(R.id.txtPlaylistTitle);
        TextView txtSongCount = view.findViewById(R.id.txtSongCount);
        TextView tvEmptyPlaylist = view.findViewById(R.id.tvEmptyPlaylist);
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerPlaylistSongs = view.findViewById(R.id.recyclerPlaylistSongs);

        recyclerPlaylistSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        SongAdapter songAdapter = new SongAdapter(new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                musicViewModel.playSong(song, null);
            }

            @Override
            public void onFavoriteClick(Song song) {
            }

            @Override
            public void onOptionsClick(Song song) {
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
            if (songs != null) {
                songAdapter.setSongs(songs);

                int cantidad = songs.size();
                txtSongCount.setText(cantidad + " " + (cantidad == 1 ? "canción" : "canciones"));

                if (cantidad == 0) {
                    tvEmptyPlaylist.setVisibility(View.VISIBLE);
                    recyclerPlaylistSongs.setVisibility(View.GONE);
                } else {
                    tvEmptyPlaylist.setVisibility(View.GONE);
                    recyclerPlaylistSongs.setVisibility(View.VISIBLE);
                }
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (newText.isEmpty()) {
                        musicViewModel.loadPlaylist(playlistId);
                    } else {
                        musicViewModel.searchInSpecificPlaylist(playlistId, newText);
                    }
                };
                handler.postDelayed(searchRunnable, 500);
                return true;
            }
        });

        musicViewModel.loadPlaylist(this.playlistId);
    }
}