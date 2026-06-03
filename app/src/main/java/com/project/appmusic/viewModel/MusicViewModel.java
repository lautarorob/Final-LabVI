package com.project.appmusic.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.media3.exoplayer.ExoPlayer;

import com.project.appmusic.Playlist;
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

    private List<Song> currentPlaybackQueue = new java.util.ArrayList<>();
    private MutableLiveData<List<Song>> listaCancionesLiveData = new MutableLiveData<>();

    private MutableLiveData<List<Song>> searchSongsLiveData = new MutableLiveData<>();

    //canal de errores
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    //estado de reproduccion
    private MutableLiveData<Song> currentSong = new MutableLiveData<>();

    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();

    private MutableLiveData<Boolean> isShuffle = new MutableLiveData<>(false);

    public static final int REPEAT_MODE_OFF = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_ONE = 2;


    public MutableLiveData<Song> getCurrentSong() {
        return currentSong;
    }

    public MutableLiveData<Boolean> getIsPlaying() {
        return isPlaying;
    }

    public MutableLiveData<List<Song>> getListaCancionesLiveData() {
        return listaCancionesLiveData;
    }

    public MutableLiveData<List<Song>> getSearchSongsLiveData() {
        return searchSongsLiveData;
    }

    public void setListaCancionesLiveData(MutableLiveData<List<Song>> listaCancionesLiveData) {
        this.listaCancionesLiveData = listaCancionesLiveData;
    }

    private final MutableLiveData<Integer> repeatMode = new MutableLiveData<>(REPEAT_MODE_OFF);

    public MutableLiveData<Boolean> getIsShuffle() {
        return isShuffle;
    }

    public MutableLiveData<Integer> getRepeatMode() {
        return repeatMode;
    }


    //getter para que la vista escuche los errores
    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void downloadSongs(String nombreCancion) {
        //llamado a la instancia de retrofit
        DeezerApiService api = RetrofitClient.getApiService();
        //consulta
        Call<DeezerListResponse<Song>> call = api.searchSongs(nombreCancion);
        //respuesta
        call.enqueue(new Callback<DeezerListResponse<Song>>() {
            @Override
            public void onResponse(Call<DeezerListResponse<Song>> call, Response<DeezerListResponse<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> canciones = response.body().getData();

                    if (!canciones.isEmpty()) {
                        searchSongsLiveData.postValue(canciones);
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


    private MutableLiveData<List<Song>> listaRegionalLiveData = new MutableLiveData<>();

    public MutableLiveData<List<Song>> getListaRegionalLiveData() {
        return listaRegionalLiveData;
    }

    public void buscarIdPorPais(String pais) {
        String terminoBusqueda = "Top 50" + pais;
        DeezerApiService api = RetrofitClient.getApiService();

        api.buscarPlaylistRegional(terminoBusqueda).enqueue(new Callback<DeezerListResponse<Playlist>>() {
            @Override
            public void onResponse(Call<DeezerListResponse<Playlist>> call, Response<DeezerListResponse<Playlist>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getData().isEmpty()) {

                    // Extraemos el número de la lista
                    long idEncontrado = response.body().getData().get(0).getId();

                    downloadRegional(idEncontrado);

                } else {
                    errorLiveData.postValue("No se encontró una playlist oficial para " + pais);
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Playlist>> call, Throwable t) {
                errorLiveData.postValue("Error al buscar el ID: " + t.getMessage());
            }
        });
    }

    public void downloadRegional(long playlistId) {
        DeezerApiService api = RetrofitClient.getApiService();

        Call<DeezerListResponse<Song>> call = api.getTopRegionalTracks(playlistId);

        call.enqueue(new Callback<DeezerListResponse<Song>>() {
            @Override
            public void onResponse(Call<DeezerListResponse<Song>> call, Response<DeezerListResponse<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Song> canciones = response.body().getData();

                    if (!canciones.isEmpty()) {
                        listaRegionalLiveData.postValue(canciones);
                    } else {
                        errorLiveData.postValue("La playlist regional está vacía.");
                    }
                } else {
                    errorLiveData.postValue("Error en la respuesta del servidor.");
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {
                errorLiveData.postValue("Error de red: " + t.getMessage());
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
    public void playSong(Song song, List<Song> currentList) {
        // Sobrescribe la cola de reproducción activa con la lista de la pantalla actual
        this.currentPlaybackQueue = currentList;

        //Continúa tu lógica normal de reproducción
        currentSong.setValue(song);
        isPlaying.setValue(true);
    }

    public void togglePlayback() {
        isPlaying.setValue(!isPlaying.getValue());
    }


    private ExoPlayer exoPlayer;

    // metodo para obtener el milisegundo exacto en el que va la cancion
    public long getCurrentPosition() {
        if (exoPlayer != null) {
            return exoPlayer.getCurrentPosition();
        }
        return 0;
    }

    // metodo para recibir el reproductor real
    public void setExoPlayer(ExoPlayer player) {
        this.exoPlayer = player;
    }

    // metodo para obtener cuánto dura el archivo cargado en total
    public long getDuration() {
        if (exoPlayer != null) {
            long duration = exoPlayer.getDuration();
            if (duration < 0) {
                return 0;
            }
            return duration;
        }
        return 0;
    }


    // Se agrega el parámetro isManualSkip
    public void playNextSong(boolean isManualSkip) {
        List<Song> currentPlaylist = currentPlaybackQueue;
        Song songPlayed = currentSong.getValue();

        int mode = repeatMode.getValue() != null ? repeatMode.getValue() : REPEAT_MODE_OFF;
        boolean shuffleActive = isShuffle.getValue() != null ? isShuffle.getValue() : false;

        if (songPlayed != null && currentPlaylist != null) {

            // PRIORIDAD ALTA: Repetir Uno
            if (mode == REPEAT_MODE_ONE) {
                if (isManualSkip) {
                    // El usuario forzo el avance se apaga el repetir 1 y pasa a repetir todo
                    repeatMode.setValue(REPEAT_MODE_ALL);
                } else {
                    // La canción termino naturalmente. Se reinicia y se aborta el salto.
                    if (exoPlayer != null) {
                        exoPlayer.seekTo(0);
                    }
                    return;
                }
            }

            int currentSongIndex = currentPlaylist.indexOf(songPlayed);

            // PRIORIDAD MEDIA: Modo Aleatorio
            if (shuffleActive && currentPlaylist.size() > 1) {
                java.util.Random random = new java.util.Random();
                int randomIndex = currentSongIndex;

                while (randomIndex == currentSongIndex) {
                    randomIndex = random.nextInt(currentPlaylist.size());
                }

                playSong(currentPlaylist.get(randomIndex), currentPlaylist);
                return;
            }

            // PRIORIDAD BAJA: Avance secuencial normal
            if (currentSongIndex != -1 && currentSongIndex < currentPlaylist.size() - 1) {
                Song nextSong = currentPlaylist.get(currentSongIndex + 1);

                playSong(nextSong, currentPlaylist);

            } else {
                // final de la playlist
                if (mode == REPEAT_MODE_ALL) {
                    Song firstSong = currentPlaylist.get(0);

                    playSong(firstSong, currentPlaylist);

                } else {
                    isPlaying.setValue(false);
                }
            }
        }
    }

    public void playPreviousSong() {
        // se evalua la posicion actual del motor de audio
        long posicionActual = getCurrentPosition();

        // si la pista avanzo mas de 3 segundos se reinicia
        if (posicionActual > 3000) {
            if (exoPlayer != null) {
                exoPlayer.seekTo(0);
            }
            return;
        }

        List<Song> currentPlaylist = currentPlaybackQueue;
        Song songPlayed = currentSong.getValue();
        if (songPlayed != null && currentPlaylist != null) {
            int currentSongIndex = currentPlaylist.indexOf(songPlayed);
            if (currentSongIndex != -1 && currentSongIndex > 0) {
                Song previousSong = currentPlaylist.get(currentSongIndex - 1);
                currentSong.setValue(previousSong);
            } else {
                currentSong.setValue(currentPlaylist.get(currentPlaylist.size() - 1));
            }
        }
    }

    public void toggleShuffle() {
        Boolean currentState = isShuffle.getValue();
        if (currentState != null) {
            isShuffle.setValue(!currentState);
        }
    }

    public void toggleRepeat() {
        Integer currentMode = repeatMode.getValue();
        if (currentMode != null) {
            repeatMode.setValue((currentMode + 1) % 3);
        }
    }


}