package com.project.appmusic.regionalFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.optionsSong.SongOptionsFragment;
import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;


public class RegionalFragment extends Fragment {

    //lanzador de permisos
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private Switch switchLocation;

    //motor de geolocalizacion (se conecta a la antena y redes)
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    private MusicViewModel musicViewModel;

    private RecyclerView recyclerSongs;


    public RegionalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // el usuario acaba de aceptar se dispara la búsqueda de hardware
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_regional, container, false);

    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchLocation = view.findViewById(R.id.switchLocation);
        fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity());
        // inicializacion del ViewModel
        musicViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(com.project.appmusic.viewModel.MusicViewModel.class);

        ColorStateList azulVivo = ColorStateList.valueOf(android.graphics.Color.parseColor("#1D74FF"));
        //ColorStateList azulOscuro = ColorStateList.valueOf(android.graphics.Color.parseColor("#0A0E17"));
        ColorStateList blanco = ColorStateList.valueOf(android.graphics.Color.parseColor("#FFFFFF"));

        recyclerSongs = view.findViewById(R.id.recyclersongs);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(requireContext()));

        musicViewModel.getListaRegionalLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (switchLocation.isChecked()) {
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
            }
        });

        musicViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMsg -> {
            Toast.makeText(requireContext(), R.string.API_state + errorMsg, Toast.LENGTH_LONG).show();
            // Desactiva el switch visualmente si hubo una falla crítica de red
            if (switchLocation != null) switchLocation.setChecked(false);
        });

        musicViewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                musicViewModel.getToastMessageLiveData().setValue(null);
            }
        });


        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) {
                return;
            }
            if (isChecked) {
                checkLocationPermission();
                switchLocation.setThumbTintList(azulVivo);
                switchLocation.setTrackTintList(blanco);
            } else {
                Toast.makeText(requireContext(), R.string.location_disabled, Toast.LENGTH_SHORT).show();
                switchLocation.setThumbTintList(blanco);
                switchLocation.setTrackTintList(blanco);
                // desvinculacion del adaptador para vaciar la lista visualmente
                if (recyclerSongs != null) {
                    recyclerSongs.setAdapter(null);
                }
            }
        });

        musicViewModel.getFavoriteIdsLiveData().observe(getViewLifecycleOwner(), favoriteIds -> {
            if (recyclerSongs.getAdapter() != null) {
                ((SongAdapter) recyclerSongs.getAdapter()).setFavoriteIds(favoriteIds);
            }
        });


        musicViewModel.loadFavoriteIds();

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // si el permiso ya esta concedido se dispara la busqueda de hardware directamente
            obtenerUbicacionActual();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void obtenerUbicacionActual() {
        // validacion doble
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // se genera el token de seguridad
        com.google.android.gms.tasks.CancellationTokenSource cancellationTokenSource = new com.google.android.gms.tasks.CancellationTokenSource();

        // se obtiene la ubicacion actual forzando la antena GPS
        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // se instancia el Geocoder con el idioma/región del dispositivo
                        android.location.Geocoder geocoder = new android.location.Geocoder(requireContext(), java.util.Locale.getDefault());

                        try {
                            // se solicita la traduccion de la ubicacion actual a una direccion
                            java.util.List<android.location.Address> direcciones = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1
                            );

                            if (direcciones != null && !direcciones.isEmpty()) {
                                android.location.Address direccionActual = direcciones.get(0);

                                // extraccion de la provincia
                                String pais = direccionActual.getCountryName();

                                if (pais != null && !pais.isEmpty()) {
                                    Toast.makeText(requireContext(), R.string.seeking_success_in + " " + pais, Toast.LENGTH_LONG).show();

                                    // puente hacia el ViewModel (le mandamos el String)
                                    musicViewModel.buscarIdPorPais(pais);

                                } else {
                                    // Falla: La base de datos no tiene una provincia para estas coordenadas
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
                        // Falla: El sensor GPS devolvio un valor vacio
                        if (switchLocation != null) switchLocation.setChecked(false);
                        Toast.makeText(requireContext(), R.string.location_could_not_be_obtained, Toast.LENGTH_SHORT).show();

                        // Abre la pantalla de ajustes de ubicación nativa de Android
                        android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                //Atrapa errores internos de Google Play Services
                .addOnFailureListener(requireActivity(), e -> {
                    if (switchLocation != null) switchLocation.setChecked(false);
                    Toast.makeText(requireContext(), R.string.sensor_error + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}