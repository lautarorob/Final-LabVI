package com.project.appmusic.likedSongsFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.R;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;


public class LikedSongsFragment extends Fragment {

    MusicViewModel musicViewModel;


    public LikedSongsFragment() {
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
        return inflater.inflate(R.layout.fragment_liked_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerFavorites = view.findViewById(R.id.recyclersongs);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        TextView tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites);

        musicViewModel.getFavoritesLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty()) {
                SongAdapter adapter = new SongAdapter(requireContext(), songs, true, song -> {
                    musicViewModel.playSong(song, songs);
                });
                recyclerFavorites.setAdapter(adapter);
                recyclerFavorites.setVisibility(View.VISIBLE);
                tvEmptyFavorites.setVisibility(View.GONE);
            }else {
                recyclerFavorites.setVisibility(View.GONE);
                tvEmptyFavorites.setVisibility(View.VISIBLE);
            }
        });
        musicViewModel.loadFavorites();
    }
}