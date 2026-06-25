package com.project.appmusic.likedSongsFragments;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;


public class LikedSongsFragment extends Fragment {

    MusicViewModel musicViewModel;

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

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
        TextView tvSongCount = view.findViewById(R.id.SongCount);

        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());


        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        TextView tvEmptyFavorites = view.findViewById(R.id.tvEmptyFavorites);

        musicViewModel.getFavoritesLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null && !songs.isEmpty()) {
                // contador con la cantidad de canciones
                int cantidad = songs.size();
                if (cantidad == 1) {
                    tvSongCount.setText(getString(R.string.song_count_single));
                } else {
                    tvSongCount.setText(getString(R.string.song_count_multiple, cantidad));
                }
                tvSongCount.setVisibility(View.VISIBLE); // Aseguramos que el contador sea visible

                SongAdapter adapter = new SongAdapter(requireContext(), songs, false, true, new SongAdapter.OnSongClickListener() {
                    @Override
                    public void onSongClick(Song song) {
                        // Qué hacer cuando tocan la canción completa
                        musicViewModel.playSong(song, songs);
                    }

                    @Override
                    public void onFavoriteClick(Song song) {
                        // Qué hacer cuando tocan solo el botón de favorito
                        musicViewModel.toggleFavorite(song);
                    }

                    @Override
                    public void onOptionsClick(Song song) {
                        // Qué hacer cuando tocan el botón de opciones
                        SongOptionsFragment songOptionsFragment = new SongOptionsFragment();
                        //pasamos la cancion seleccionada
                        songOptionsFragment.setSong(song);
                        //mostramos el fragmento
                        songOptionsFragment.show(getParentFragmentManager(), "songOptions");
                    }

                    @Override
                    public void onRemoveFromPlaylistClick(Song song) {
                        // No es necesario en este caso
                    }
                });

                java.util.List<Long> currentIds = musicViewModel.getFavoriteIdsLiveData().getValue();
                if (currentIds != null) {
                    adapter.setFavoriteIds(currentIds);
                }
                recyclerFavorites.setAdapter(adapter);
                recyclerFavorites.setVisibility(View.VISIBLE);
                tvEmptyFavorites.setVisibility(View.GONE);
            } else {
                recyclerFavorites.setVisibility(View.GONE);
                tvEmptyFavorites.setVisibility(View.VISIBLE);
                tvSongCount.setVisibility(View.GONE); // Ocultamos el contador si la lista queda vacía
            }
        });

        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (recyclerFavorites.getAdapter() != null) {
                ((SongAdapter) recyclerFavorites.getAdapter()).setFavoriteIds(favoriteIds);
            }
        });

        musicViewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                musicViewModel.getToastMessageLiveData().setValue(null);
            }
        });

        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Realiza la búsqueda cuando se envía la consulta
                if (query != null && !query.trim().isEmpty()) {
                    musicViewModel.searchInFavorites(query);

                    // Limpia el campo de búsqueda
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // reinicio de cronometro actual si el usuario escribe
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                // valida que el texto no esté vacío
                if (newText != null && !newText.trim().isEmpty()) {

                    // actualizacion de tarea
                    searchRunnable = () -> {
                        musicViewModel.searchInFavorites(newText);
                    };

                    // inicio de cronometro
                    handler.postDelayed(searchRunnable, 500);

                } else {
                    musicViewModel.loadFavorites();
                }
                return true;
            }
        });


        //carga de canciones completas
        musicViewModel.loadFavorites();
        //carga en memoria de los corazones
        musicViewModel.loadFavoriteIds();
    }
}