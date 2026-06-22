package com.project.appmusic.toolbarFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.MainActivity;
import com.project.appmusic.R;
import com.project.appmusic.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

public class SearchFragment extends Fragment {

    private MusicViewModel musicViewModel;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //vinculación de vistas
        androidx.appcompat.widget.SearchView searchView = view.findViewById(R.id.search_view);
        RecyclerView recyclerSongs = view.findViewById(R.id.recyclersongs);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        musicViewModel.getSearchSongsLiveData().observe(getViewLifecycleOwner(), songs -> {

            SongAdapter adapter = new SongAdapter(requireContext(), songs, false, true, new SongAdapter.OnSongClickListener() {
                @Override
                public void onSongClick(Song song) {
                    musicViewModel.playSong(song, songs);
                }

                @Override
                public void onFavoriteClick(Song song) {
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

            recyclerSongs.setAdapter(adapter);
        });

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
        }

        musicViewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                musicViewModel.getToastMessageLiveData().setValue(null);
            }
        });

        //configuración de la barra de búsqueda
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Realiza la búsqueda cuando se envía la consulta
                if (query != null && !query.trim().isEmpty()) {
                    musicViewModel.downloadSongs(query);

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
                        // disparo de peticion a Retrofit
                        musicViewModel.downloadSongs(newText);
                    };

                    // inicio de cronometro
                    handler.postDelayed(searchRunnable, 500);

                } else {
                    recyclerSongs.setAdapter(null);
                }
                return true;
            }
        });

        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (recyclerSongs.getAdapter() != null) {
                ((SongAdapter) recyclerSongs.getAdapter()).setFavoriteIds(favoriteIds);
            }
        });


        musicViewModel.loadFavoriteIds();

    }
}