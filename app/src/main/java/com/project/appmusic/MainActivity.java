package com.project.appmusic;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
/*
        //llamado a la instancia de retrofit
        DeezerApiService api = RetrofitClient.getApiService();

        //consulta de prueba
        Call<DeezerListResponse<Song>> call = api.searchSongs("daft punk");

        call.enqueue(new Callback<DeezerListResponse<Song>>() {
            @Override
            public void onResponse(Call<DeezerListResponse<Song>> call, Response<DeezerListResponse<Song>> response) {
                // Verificacion de código 200 OK
                if (response.isSuccessful() && response.body() != null) {

                    // extraccion de la lista de la envoltura
                    List<Song> canciones = response.body().getData();

                    if (!canciones.isEmpty()) {
                        // primera canción de la lista
                        Song primeraCancion = canciones.get(0);

                        // impresion en consola para verificar
                        Log.d("API_TEST", "¡Conexión Exitosa!");
                        Log.d("API_TEST", "Título descargado: " + primeraCancion.getTitulo());
                        Log.d("API_TEST", "URL del Audio: " + primeraCancion.getUrlAudio());
                        Log.d("API_TEST", "URL de la Portada: " + primeraCancion.getUrlPortada());
                    }
                } else {
                    Log.e("API_TEST", "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {// Este bloque se ejecuta si no hay WiFi, no hay permisos, o el JSON es inválido
                Log.e("API_TEST", "Falla crítica de red: " + t.getMessage());
            }
        });*/


    }
}