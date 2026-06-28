package com.project.appmusic.toolbarFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.project.appmusic.R;
import com.project.appmusic.data.entity.PlaylistWithTracks;
import com.project.appmusic.likedSongsFragments.LikedSongsFragment;
import com.project.appmusic.playlistCreation.CreatePlaylistFragment;
import com.project.appmusic.dialogs.DialogPlaylistDeleteFragment;
import com.project.appmusic.playlistCreation.PlayListFragment;
import com.project.appmusic.reciclerView.PlaylistAdapter;
import com.project.appmusic.viewModel.MusicViewModel;


public class LibraryFragment extends Fragment {

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable searchRunnable;

    public LibraryFragment() {
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
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    MusicViewModel musicViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);
        RecyclerView recyclerPlaylists = view.findViewById(R.id.recyclerLibrary);
        recyclerPlaylists.setLayoutManager(new LinearLayoutManager(requireContext()));

        LinearLayout btnCreatePlaylist = view.findViewById(R.id.btnCreateNewPlaylist);

        // Instanciación del adaptador
        PlaylistAdapter adapter = new PlaylistAdapter(
                false,

                //  Click normal
                playlistSelect -> {
                    // Validación de tipo de playlist
                    if (playlistSelect.playlist.isFavorites) {
                        // Instanciación de vista de sistema
                        LikedSongsFragment likedSongsFragment = new LikedSongsFragment();
                        // Transacción estructural
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_fullscreen_container, likedSongsFragment)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        // Instanciación de vista de usuario
                        PlayListFragment playListFragment = new PlayListFragment();
                        // Inyección de dependencia (ID)
                        playListFragment.setPlaylistId(playlistSelect.playlist.playlistId);
                        // Transacción estructural
                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.main_fullscreen_container, playListFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                },

                //  Click largo
                playlistSelect -> {
                    // Bloqueo de seguridad: Evitar que se borre la lista del sistema (Favoritos)
                    if (playlistSelect.playlist.isFavorites) {
                        Toast.makeText(requireContext(), R.string.you_cannot_delete_your_favorites_list, Toast.LENGTH_SHORT).show();
                        return; // Detiene la ejecución
                    }

                    // Instanciación del cartel flotante pasándole los datos de la lista seleccionada
                    DialogPlaylistDeleteFragment dialog = new DialogPlaylistDeleteFragment(
                            playlistSelect.playlist.playlistId,
                            playlistSelect.playlist.name
                    );

                    // Despliegue del DialogFragment sobre la vista actual
                    dialog.show(getParentFragmentManager(), "DeletePlaylistDialog");
                }
        );

        // Vincular el adaptador a la vista
        recyclerPlaylists.setAdapter(adapter);

        //  Observar la base de datos y pintar las filas (se fuerza la creacion de la playlist "me gusta")
        musicViewModel.getUserPlaylistsLiveData().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                // Creamos una copia de la lista para no mutar los datos originales de Room
                java.util.List<PlaylistWithTracks> listaProcesada = new java.util.ArrayList<>(playlists);

                //  Verificamos si la base de datos ya trajo la lista de favoritos
                boolean existeFavoritos = false;
                for (PlaylistWithTracks p : listaProcesada) {
                    if (p.playlist.isFavorites) {
                        existeFavoritos = true;
                        break;
                    }
                }

                // Si no existe (usuario nuevo o lista vacía), la inyectamos artificialmente
                if (!existeFavoritos) {
                    PlaylistWithTracks favFantasma = new PlaylistWithTracks();
                    favFantasma.playlist = new com.project.appmusic.data.entity.PlaylistEntity();
                    favFantasma.playlist.name = getString(R.string.your_likes);
                    favFantasma.playlist.isFavorites = true;
                    favFantasma.playlist.playlistId = -1; // Un ID negativo para indicar que es del sistema

                    // La insertamos en la posición 0 para que siempre salga arriba
                    listaProcesada.add(0, favFantasma);
                }

                //  lista parcheada al adaptador
                adapter.setPlaylists(listaProcesada);
            }
        });


        btnCreatePlaylist.setOnClickListener(v -> {
            CreatePlaylistFragment createPlaylistFragment = new CreatePlaylistFragment();

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fullscreen_container, createPlaylistFragment)
                    .addToBackStack(null)
                    .commit();
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


        musicViewModel.loadUserPlaylists();
    }


}