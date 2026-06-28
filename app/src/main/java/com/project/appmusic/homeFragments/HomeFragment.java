package com.project.appmusic.homeFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerSongs;
    private MusicViewModel musicViewModel;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //vinculación de vistas
        recyclerSongs = view.findViewById(R.id.recyclersongs);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        //inicializacion viewmodel
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        // observador de canciones: pone las canciones en la pantalla
        musicViewModel.getListaCancionesLiveData().observe(getViewLifecycleOwner(), songs -> {

            SongAdapter adapter = new SongAdapter(requireContext(), songs, true, true, new SongAdapter.OnSongClickListener() {
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

            recyclerSongs.setAdapter(adapter);
        });

        //disparo de la peticion de red
        // musicViewModel.descargarCanciones("eminem");
        musicViewModel.downloadTopGlobal();

        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (recyclerSongs.getAdapter() != null) {
                ((SongAdapter) recyclerSongs.getAdapter()).setFavoriteIds(favoriteIds);
            }
        });

        // Disparo de consulta asíncrona al cargar el Fragmento
        musicViewModel.loadFavoriteIds();


    }
}