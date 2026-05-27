package com.project.appmusic.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.appmusic.Song;
import com.project.appmusic.api.DeezerApiService;
import com.project.appmusic.api.DeezerListResponse;
import com.project.appmusic.api.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicViewModel extends ViewModel {
    //canal de datos
    private MutableLiveData<List<Song>> listaCancionesLiveData = new MutableLiveData<>();
    //canal de errores
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    //estado de reproduccion
    private MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();

    public MutableLiveData<Song> getCurrentSong() {
        return currentSong;
    }

    public MutableLiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public MutableLiveData<List<Song>> getListaCancionesLiveData() {
        return listaCancionesLiveData;
    }

    //getter para que la vista escuche los errores
    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void downloadSongs(String nombreArtista) {
        //llamado a la instancia de retrofit
        DeezerApiService api = RetrofitClient.getApiService();
        //consulta
        Call<DeezerListResponse<Song>> call = api.searchSongs(nombreArtista);
        //respuesta
        call.enqueue(new Callback<DeezerListResponse<Song>>() {
            @Override
            public void onResponse(Call<DeezerListResponse<Song>> call, Response<DeezerListResponse<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> canciones = response.body().getData();

                    if (!canciones.isEmpty()) {
                        listaCancionesLiveData.postValue(canciones);
                    } else {
                        // Lista vacia
                        errorLiveData.postValue("Sin Resultados.");
                    }
                } else {
                    // falla de servidor
                    errorLiveData.postValue("Error en la respuesta del servidor.");
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {
                // falla de red
                errorLiveData.postValue("Error de red");
            }
        });
    }


    public void downloadTopGlobal() {
        DeezerApiService api = RetrofitClient.getApiService();

        Call<DeezerListResponse<Song>> call = api.getTopGlobalTracks();

        call.enqueue(new Callback<DeezerListResponse<Song>>() {
            @Override
            public void onResponse(Call<DeezerListResponse<Song>> call, Response<DeezerListResponse<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> canciones = response.body().getData();

                    if (!canciones.isEmpty()) {
                        listaCancionesLiveData.postValue(canciones);
                    } else {
                        errorLiveData.postValue("El chart global está vacío.");
                    }
                } else {
                    errorLiveData.postValue("Error en la respuesta del servidor.");
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {
                errorLiveData.postValue("Error de red");
            }
        });
    }


    //metodos para reproducir la cancion
    public void playSong(Song song) {
        currentSong.setValue(song);
        isPlaying.setValue(true);
    }

    public void togglePlayback() {
        isPlaying.setValue(!isPlaying.getValue());
    }


}