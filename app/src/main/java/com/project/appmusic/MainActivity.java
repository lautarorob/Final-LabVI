package com.project.appmusic;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.exoplayer.ExoPlayer;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.appmusic.homeFragments.HomeFragment;
import com.project.appmusic.toolbarFragments.LibraryFragment;
import com.project.appmusic.toolbarFragments.SearchFragment;
import com.project.appmusic.viewModel.MusicViewModel;

public class MainActivity extends AppCompatActivity {

    private ExoPlayer exoPlayer;
    private MusicViewModel musicViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //inicializacion viewmodel
        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fullscreen_container, new HomeFragment())
                    .commit();
        }

        //observador de cancion actual
        musicViewModel.getCurrentSong().observe(this, song -> {
            //se delega la carga de la url
            preparePlayer(song.getUrlAudio());
        });

        //observador de estado de reproduccion
        musicViewModel.getIsPlaying().observe(this, isPlaying -> {
            if (exoPlayer != null) {
                if (isPlaying) {
                    exoPlayer.play();
                } else {
                    exoPlayer.pause();
                }
            }
        });

        //barra de herramientas inferior

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragmentSeleccionado = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragmentSeleccionado = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                fragmentSeleccionado = new SearchFragment();
            } else if (itemId == R.id.nav_library) {
                fragmentSeleccionado = new LibraryFragment();
            }

            if (fragmentSeleccionado != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_fullscreen_container, fragmentSeleccionado)
                        .addToBackStack(null)
                        .commit();

                // Retornar true es obligatorio para que el ícono cambie de color visualmente
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release(); // apaga y destruye el reproductor
            exoPlayer = null;
        }
    }

    private void preparePlayer(String urlAudio) {
        if (exoPlayer == null) {
            exoPlayer = new ExoPlayer.Builder(this).build();
            musicViewModel.setExoPlayer(exoPlayer);
            //observador de cambio de cancion
            exoPlayer.addListener(new ExoPlayer.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        // false porque terminó naturalmente por tiempo
                        musicViewModel.playNextSong(false);
                    }
                }
            });
        } else {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }

        // objetos de exoPlayer "MediaItem" para cargar las URLs
        androidx.media3.common.MediaItem mediaItem = androidx.media3.common.MediaItem.fromUri(urlAudio);
        exoPlayer.setMediaItem(mediaItem);

        // prepara el audio en segundo plano automáticamente
        exoPlayer.prepare();

        // si el ViewModel dice que debería estar sonando, le damos play
        if (Boolean.TRUE.equals(musicViewModel.getIsPlaying().getValue())) {
            exoPlayer.play();
        }

    }

    public void setMiniPlayerVisibility(boolean isVisible) {
        View miniPlayerContainer = findViewById(R.id.fragment_mini_player);
        if (miniPlayerContainer != null) {
            miniPlayerContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }
}