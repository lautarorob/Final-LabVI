package com.project.appmusic;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.appmusic.reciclerView.SongAdapter;
import com.project.appmusic.viewModel.MusicViewModel;

public class MainActivity extends AppCompatActivity {

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

        //suscripcion al canal de datos
        musicViewModel.getListaCancionesLiveData().observe(this, songs -> {
            //cuando la data real llega creamos el adaptador y lo acoplamos
            SongAdapter songAdapter = new SongAdapter(MainActivity.this, songs);
            recyclerSongs.setAdapter(songAdapter);
        });

        //suscripcion al canal de errores
        musicViewModel.getErrorLiveData().observe(this, mensajeError -> {
            //cuando llega el mensaje de error se muestran
            Toast.makeText(MainActivity.this, mensajeError, Toast.LENGTH_SHORT).show();
        });

        //disparo de la peticion de red
        // musicViewModel.descargarCanciones("eminem");

        musicViewModel.descargarTopGlobal();
    }
}