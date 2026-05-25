package com.project.appmusic;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

public class MainActivity extends AppCompatActivity {


    private ExoPlayer exoPlayer;
    private RecyclerView recyclerSongs;
    private MusicViewModel musicViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //vinculación de vistas
        recyclerSongs = findViewById(R.id.recyclersongs);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(this));

        //inicializacion viewmodel
        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        // observador de canciones: pone las canciones en la pantalla
        musicViewModel.getListaCancionesLiveData().observe(this, songs -> {
            SongAdapter adapter = new SongAdapter(this, songs, song -> {
                // Aquí simplemente avisamos al ViewModel: "El usuario tocó esta"
                musicViewModel.playSong(song);
            });
            recyclerSongs.setAdapter(adapter);
        });

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


        //suscripcion al canal de errores
        musicViewModel.getErrorLiveData().observe(this, mensajeError -> {
            //cuando llega el mensaje de error se muestran
            Toast.makeText(MainActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
        });

        //disparo de la peticion de red
        // musicViewModel.descargarCanciones("eminem");

        musicViewModel.downloadTopGlobal();
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
        }else{
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

}