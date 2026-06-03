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
            SongAdapter adapter = new SongAdapter(requireContext(), songs, true, song -> {
                musicViewModel.playSong(song, songs);
            });
            recyclerSongs.setAdapter(adapter);
        });

        //suscripcion al canal de errores
        musicViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), mensajeError -> {
            //cuando llega el mensaje de error se muestran
            Toast.makeText(requireContext(), mensajeError, Toast.LENGTH_SHORT).show();
        });

        //disparo de la peticion de red
        // musicViewModel.descargarCanciones("eminem");
        musicViewModel.downloadTopGlobal();


    }
}