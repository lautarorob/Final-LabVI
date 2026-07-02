package com.project.appmusic.regionalFragments;


import android.Manifest;

import android.content.pm.PackageManager;

import android.content.res.ColorStateList;

import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.ImageView;

import android.widget.Switch;

import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;

import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;


import com.project.appmusic.R;

import com.project.appmusic.objetos.Song;

import com.project.appmusic.optionsSong.SongOptionsFragment;

import com.project.appmusic.reciclerView.SongAdapter;

import com.project.appmusic.viewModel.MusicViewModel;


import java.util.ArrayList;

import java.util.List;


public class RegionalFragment extends Fragment {


    private ActivityResultLauncher<String> requestPermissionLauncher;


    private Switch switchLocation;

    private androidx.appcompat.widget.SearchView searchView;

    private ImageView btnBack;

    private RecyclerView recyclerSongs;


    private SongAdapter songAdapter;


    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    private MusicViewModel musicViewModel;


    private List<Song> originalRegionalSongs = new ArrayList<>();

    private android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

    private Runnable searchRunnable;


    public RegionalFragment() {

// Required empty public constructor

    }


    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        obtenerUbicacionActual();
                    } else {
                        if (switchLocation != null) {
                            switchLocation.setChecked(false);
                        }
                        Toast.makeText(
                                requireContext(),
                                getString(R.string.err_location_required),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

    }


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_regional, container, false);
    }


    @Override

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchLocation = view.findViewById(R.id.switchLocation);
        btnBack = view.findViewById(R.id.btnBack);
        searchView = view.findViewById(R.id.search_view);
        recyclerSongs = view.findViewById(R.id.recyclersongs);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchView.setVisibility(View.GONE);
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());

        musicViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        ColorStateList azulVivo = ColorStateList.valueOf(android.graphics.Color.parseColor("#1D74FF"));

        ColorStateList blanco = ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF"));

        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        songAdapter = new SongAdapter(requireContext(), new ArrayList<>(), true, true, new SongAdapter.OnSongClickListener() {

            @Override
            public void onSongClick(Song song) {
                musicViewModel.playSong(song, originalRegionalSongs);
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
            }

        });

        recyclerSongs.setAdapter(songAdapter);


        musicViewModel.getListaRegionalLiveData().observe(getViewLifecycleOwner(), songs -> {

            if (songs != null && !songs.isEmpty()) {
                if (!switchLocation.isChecked()) {
                    switchLocation.setChecked(true);
                }
                originalRegionalSongs = songs;
                searchView.setVisibility(View.VISIBLE);
                songAdapter.setSongs(songs);
            } else {

                searchView.setVisibility(View.GONE);
                originalRegionalSongs.clear();

                if (songAdapter != null) {
                    songAdapter.setSongs(new ArrayList<>());
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

                searchRunnable = () -> filtrarCancionesEnMemoria(newText);
                handler.postDelayed(searchRunnable, 300);
                return true;
            }

        });


        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                switchLocation.setThumbTintList(azulVivo);
                switchLocation.setTrackTintList(blanco);
                // Solo dispara la antena GPS si el usuario lo tocó con el dedo

                if (buttonView.isPressed()) {
                    checkLocationPermission();
                }

            } else {

                switchLocation.setThumbTintList(blanco);
                switchLocation.setTrackTintList(blanco);

                // Si el usuario lo apaga manualmente, limpiamos el ViewModel para borrar la memoria caché
                if (buttonView.isPressed()) {
                    musicViewModel.getListaRegionalLiveData().postValue(new ArrayList<>());
                    Toast.makeText(requireContext(), R.string.location_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        });


        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {

            if (songAdapter != null) {
                songAdapter.setFavoriteIds(favoriteIds);
            }
        });


        musicViewModel.loadFavoriteIds();
    }


    private void filtrarCancionesEnMemoria(String query) {

        if (query.isEmpty()) {
            songAdapter.setSongs(originalRegionalSongs);
            return;
        }


        List<Song> filteredList = new ArrayList<>();
        String queryLower = query.toLowerCase();


        for (Song song : originalRegionalSongs) {
            boolean matchesTitle = song.getTitulo() != null && song.getTitulo().toLowerCase().contains(queryLower);
            boolean matchesArtist = song.getNameArtist() != null && song.getNameArtist().toLowerCase().contains(queryLower);

            if (matchesTitle || matchesArtist) {
                filteredList.add(song);
            }
        }

        songAdapter.setSongs(filteredList);

    }

    private void checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacionActual();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void obtenerUbicacionActual() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        com.google.android.gms.tasks.CancellationTokenSource cancellationTokenSource = new com.google.android.gms.tasks.CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());

                        try {
                            List<android.location.Address> direcciones = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1
                            );

                            if (direcciones != null && !direcciones.isEmpty()) {
                                android.location.Address direccionActual = direcciones.get(0);
                                String pais = direccionActual.getCountryName();

                                if (pais != null && !pais.isEmpty()) {
                                    Toast.makeText(requireContext(), getString(R.string.seeking_success_in) + " " + pais, Toast.LENGTH_LONG).show();
                                    musicViewModel.buscarIdPorPais(pais);
                                } else {
                                    if (switchLocation != null) switchLocation.setChecked(false);
                                    Toast.makeText(
                                            requireContext(),
                                            R.string.the_exact_province_could_not_be_determined,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                            if (switchLocation != null) switchLocation.setChecked(false);
                            Toast.makeText(requireContext(), R.string.error_translate_location, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        if (switchLocation != null) switchLocation.setChecked(false);
                        Toast.makeText(requireContext(), R.string.location_could_not_be_obtained, Toast.LENGTH_SHORT).show();
                        android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })

                .addOnFailureListener(requireActivity(), e -> {
                    if (switchLocation != null) switchLocation.setChecked(false);
                    Toast.makeText(requireContext(), getString(R.string.sensor_error) + " " + e.getMessage(), Toast.LENGTH_LONG).show();

                });

    }

} 