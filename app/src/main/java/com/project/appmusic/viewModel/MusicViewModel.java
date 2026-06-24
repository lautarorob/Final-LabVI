package com.project.appmusic.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.media3.exoplayer.ExoPlayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.project.appmusic.objetos.Playlist;
import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.api.DeezerApiService;
import com.project.appmusic.api.DeezerListResponse;
import com.project.appmusic.api.RetrofitClient;
import com.project.appmusic.data.dao.PlaylistDao;
import com.project.appmusic.data.database.AppDatabase;
import com.project.appmusic.data.entity.PlaylistEntity;
import com.project.appmusic.data.entity.PlaylistTrackCrossRef;
import com.project.appmusic.data.entity.PlaylistWithTracks;
import com.project.appmusic.data.entity.TrackEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicViewModel extends AndroidViewModel {

    //canal de datos

    private List<Song> currentPlaybackQueue = new java.util.ArrayList<>();
    private MutableLiveData<List<Song>> listaCancionesLiveData = new MutableLiveData<>();

    private MutableLiveData<List<Song>> searchSongsLiveData = new MutableLiveData<>();

    //canal de errores
    private MutableLiveData<Integer> errorLiveData = new MutableLiveData<>();

    //estado de reproduccion
    private MutableLiveData<Song> currentSong = new MutableLiveData<>();

    private MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();

    private MutableLiveData<Boolean> isShuffle = new MutableLiveData<>(false);

    private MutableLiveData<List<Song>> favoritesLiveData = new MutableLiveData<>();

    public MutableLiveData<List<Song>> getFavoritesLiveData() {
        return favoritesLiveData;
    }

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
    public MutableLiveData<Integer> getErrorLiveData() {
        return errorLiveData;
    }

    private final PlaylistDao playlistDao;

    public MusicViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.playlistDao = db.playlistDao();
        this.executorService = Executors.newSingleThreadExecutor();
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
                        errorLiveData.setValue(R.string.no_results);
                    }
                } else {
                    // falla de servidor
                    errorLiveData.postValue(R.string.error_server_response);
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {
                // falla de red
                errorLiveData.postValue(R.string.network_error);
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
                    errorLiveData.postValue(R.string.no_official_playlist_found);
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Playlist>> call, Throwable t) {
                errorLiveData.postValue(R.string.error_searching_id);
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
                        errorLiveData.postValue(R.string.regional_playlist_empty);
                    }
                } else {
                    errorLiveData.postValue(R.string.error_server_response);
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {
                errorLiveData.postValue(R.string.network_error);
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
                        errorLiveData.postValue(R.string.no_songs_found);
                    }
                } else {
                    errorLiveData.postValue(R.string.error_server_response);
                }
            }

            @Override
            public void onFailure(Call<DeezerListResponse<Song>> call, Throwable t) {
                errorLiveData.postValue(R.string.network_error);
            }
        });
    }


    //metodos para reproducir la cancion
    public void playSong(Song song, List<Song> currentList) {

        // Si la canción no tiene URL de audio, significa que viene de Room
        if (song.getUrlAudio() == null || song.getUrlAudio().isEmpty()) {

            DeezerApiService api = RetrofitClient.getApiService();
            api.getTrackById(song.getId()).enqueue(new Callback<Song>() {
                @Override
                public void onResponse(Call<Song> call, Response<Song> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        // URL fresca de la API
                        String urlFresh = response.body().getUrlAudio();

                        // actualizacion de objeto en memoria
                        song.setUrlAudio(urlFresh);

                        // reproducción real
                        executeInternalPlayback(song, currentList);
                    } else {
                        errorLiveData.postValue(R.string.error_server_response);
                    }
                }

                @Override
                public void onFailure(Call<Song> call, Throwable t) {
                    errorLiveData.postValue(R.string.network_error);
                }
            });

        } else {
            // Si ya tenía la URL (viene de una búsqueda normal), reproducimos directamente
            executeInternalPlayback(song, currentList);
        }
    }

    private void executeInternalPlayback(Song song, List<Song> currentList) {
        this.currentPlaybackQueue = currentList;
        currentSong.setValue(song);
        isPlaying.setValue(true);
        checkIsFavorite(song.getId());
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


    public void playNextSong(boolean isManualSkip) {
        List<Song> currentPlaylist = currentPlaybackQueue;
        Song songPlayed = currentSong.getValue();

        int mode = repeatMode.getValue() != null ? repeatMode.getValue() : REPEAT_MODE_OFF;
        boolean shuffleActive = isShuffle.getValue() != null ? isShuffle.getValue() : false;

        if (songPlayed != null && currentPlaylist != null) {

            // PRIORIDAD 1: Repetir Uno
            if (mode == REPEAT_MODE_ONE) {
                if (isManualSkip) {
                    // El usuario forzó el avance: se apaga el repetir 1 y pasa a repetir todo
                    repeatMode.setValue(REPEAT_MODE_ALL);
                } else {
                    // La canción terminó naturalmente: se reinicia la pista
                    if (exoPlayer != null) {
                        exoPlayer.seekTo(0);
                    }
                    return;
                }
            }

            // PRIORIDAD 2: Cola Manual (FIFO)
            if (!manualQueue.isEmpty()) {
                // .remove(0) extrae la canción más antigua de la cola y la borra de la lista temporal
                Song nextQueuedSong = manualQueue.remove(0);

                // Se reproduce manteniendo el contexto de la playlist actual de fondo
                playSong(nextQueuedSong, currentPlaylist);
                return;
            }

            // Búsqueda del índice actual iterando por ID
            int currentSongIndex = -1;
            for (int i = 0; i < currentPlaylist.size(); i++) {
                if (currentPlaylist.get(i).getId() == songPlayed.getId()) {
                    currentSongIndex = i;
                    break;
                }
            }

            // PRIORIDAD 3: Modo Aleatorio
            if (shuffleActive && currentPlaylist.size() > 1) {
                java.util.Random random = new java.util.Random();
                int randomIndex = currentSongIndex;

                while (randomIndex == currentSongIndex) {
                    randomIndex = random.nextInt(currentPlaylist.size());
                }

                playSong(currentPlaylist.get(randomIndex), currentPlaylist);
                return;
            }

            // PRIORIDAD 4: Avance secuencial normal
            if (currentSongIndex != -1 && currentSongIndex < currentPlaylist.size() - 1) {
                Song nextSong = currentPlaylist.get(currentSongIndex + 1);
                playSong(nextSong, currentPlaylist);

            } else {
                // Final de la playlist
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

    private final ExecutorService executorService;

    // Canal para notificar a la vista si la canción actual es favorita o no
    private MutableLiveData<Boolean> isCurrentFavorite = new MutableLiveData<>(false);

    public MutableLiveData<Boolean> getIsCurrentFavorite() {
        return isCurrentFavorite;
    }

    //para toasts
    public MutableLiveData<Integer> toastMessageLiveData = new MutableLiveData<>();

    public MutableLiveData<Integer> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    public void toggleFavorite(Song currentSong) {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) return;

        executorService.execute(() -> {
            // se recupera o se crea la playlist "Me Gusta"
            PlaylistWithTracks favPlaylist = playlistDao.getFavoritesPlaylist(currentUserId);
            int playlistId;

            if (favPlaylist == null) {
                PlaylistEntity newPlaylist = new PlaylistEntity();
                newPlaylist.name = "I like";
                newPlaylist.userId = currentUserId;
                newPlaylist.isFavorites = true;
                playlistId = (int) playlistDao.insertPlaylist(newPlaylist);
            } else {
                playlistId = favPlaylist.playlist.playlistId;
            }

            // se verifica la relacion entre la cancion actual y la playlist
            int exist = playlistDao.isTrackInPlaylist(playlistId, currentSong.getId());

            PlaylistTrackCrossRef relacion = new PlaylistTrackCrossRef();
            relacion.playlistId = playlistId;
            relacion.deezerId = currentSong.getId();

            // Lógica Toggle (Alternador)
            if (exist > 0) {
                // Si ya estaba, la borramos
                playlistDao.deleteTrackFromPlaylist(relacion);
                isCurrentFavorite.postValue(false);
                toastMessageLiveData.postValue(R.string.removed_from_favorites);

                loadFavorites();
                loadFavoriteIds();
            } else {
                // Si no estaba, se guarda
                TrackEntity newTrack = new TrackEntity();
                newTrack.deezerId = currentSong.getId();
                newTrack.title = currentSong.getTitulo();
                newTrack.artistName = currentSong.getNameArtist();
                newTrack.coverUrl = currentSong.getUrlPortada();

                playlistDao.insertTrack(newTrack);
                playlistDao.insertTrackIntoPlaylist(relacion);
                isCurrentFavorite.postValue(true);
                toastMessageLiveData.postValue(R.string.added_to_favorites);

                loadFavorites();
                loadFavoriteIds();
            }
        });
    }

    //metodo para cargar canciones favoritas
    public void loadFavorites() {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) {
            errorLiveData.postValue(R.string.err_user_not_found);
            return;
        }

        executorService.execute(() -> {
            //buscar la playlist "Me gusta" del usuario (consulta relacional)
            PlaylistWithTracks favPlaylist = playlistDao.getFavoritesPlaylist(currentUserId);

            List<Song> songsForTheView = new ArrayList<>();

            // mapeo de la lista
            if (favPlaylist != null && favPlaylist.tracks != null) {
                // Recorrer la lista de canciones favoritas
                for (TrackEntity entity : favPlaylist.tracks) {

                    Song songMap = new Song();
                    songMap.setId(entity.deezerId);
                    songMap.setTitulo(entity.title);

                    Song.Artist artistMap = new Song.Artist();
                    artistMap.name = entity.artistName;
                    songMap.setArtist(artistMap);

                    Song.Album albumMap = new Song.Album();
                    albumMap.coverUrl = entity.coverUrl;
                    songMap.setAlbum(albumMap);

                    songsForTheView.add(songMap);
                }
            }
            // notificar a la vista
            favoritesLiveData.postValue(songsForTheView);
        });
    }


    public void checkIsFavorite(long songId) {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) return;

        executorService.execute(() -> {
            // se busca la lista de favoritos del usuario
            PlaylistWithTracks favPlaylist = playlistDao.getFavoritesPlaylist(currentUserId);

            if (favPlaylist != null) {
                // se verifica si el ID de la canción está vinculado a esta playlist
                int existe = playlistDao.isTrackInPlaylist(favPlaylist.playlist.playlistId, songId);

                // Si existe > 0, es true. Si es 0, es false.
                isCurrentFavorite.postValue(existe > 0);
            } else {
                // Si la playlist ni siquiera existe, lógicamente no es favorita
                isCurrentFavorite.postValue(false);
            }
        });
    }


    private MutableLiveData<List<Long>> favoriteIdsLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Long>> getFavoriteIdsLiveData() {
        return favoriteIdsLiveData;
    }

    public void loadFavoriteIds() {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);
        if (currentUserId == -1) return;

        executorService.execute(() -> {
            PlaylistWithTracks favPlaylist = playlistDao.getFavoritesPlaylist(currentUserId);
            List<Long> ids = new ArrayList<>();

            if (favPlaylist != null && favPlaylist.tracks != null) {
                for (TrackEntity track : favPlaylist.tracks) {
                    ids.add(track.deezerId);
                }
            }
            favoriteIdsLiveData.postValue(ids);
        });
    }

    public void searchInFavorites(String newText) {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) {
            errorLiveData.postValue(R.string.err_user_not_found);
            return;
        }


        executorService.execute(() -> {

            List<TrackEntity> searchResults = playlistDao.searchInFavoritesPlaylist(currentUserId, newText);

            List<Song> songsForTheView = new ArrayList<>();

            if (searchResults != null) {
                for (TrackEntity entity : searchResults) {
                    Song songMap = new Song();
                    songMap.setId(entity.deezerId);
                    songMap.setTitulo(entity.title);

                    Song.Artist artistMap = new Song.Artist();
                    artistMap.name = entity.artistName;
                    songMap.setArtist(artistMap);

                    Song.Album albumMap = new Song.Album();
                    albumMap.coverUrl = entity.coverUrl;
                    songMap.setAlbum(albumMap);

                    songsForTheView.add(songMap);
                }
            }

            favoritesLiveData.postValue(songsForTheView);
        });
    }

    //listado de playlists del usuario
    private MutableLiveData<List<PlaylistWithTracks>> userPlaylistsLiveData = new MutableLiveData<>();

    public LiveData<List<PlaylistWithTracks>> getUserPlaylistsLiveData() {
        return userPlaylistsLiveData;
    }

    //metodo para cargar las playlists del usuario desde la base de datos
    public void loadUserPlaylists() {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) return;

        executorService.execute(() -> {
            // Llamas a la nueva consulta relacional
            List<PlaylistWithTracks> playlists = playlistDao.getUserPlaylistsWithTracks(currentUserId);
            userPlaylistsLiveData.postValue(playlists);
        });
    }


    //  metodo para el panel deslizable (Usa la ID de la lista existente)
    public void addSongToExistingPlaylist(int playlistId, Song song) {
        executorService.execute(() -> {
            TrackEntity trackEntity = new TrackEntity();
            trackEntity.deezerId = song.getId();
            trackEntity.title = song.getTitulo();
            trackEntity.artistName = song.getNameArtist();
            trackEntity.coverUrl = song.getUrlPortada();

            playlistDao.insertTrack(trackEntity);

            PlaylistTrackCrossRef crossRef = new PlaylistTrackCrossRef();
            crossRef.playlistId = playlistId; // Usa la ID que le pasaste
            crossRef.deezerId = song.getId();

            playlistDao.insertTrackIntoPlaylist(crossRef);

            checkIsFavorite(song.getId()); // Actualiza el corazón de la canción actual
            loadFavorites();               // Actualiza la lista interna de "Me gusta"
            loadFavoriteIds();             // Actualiza los corazones

            // notificar a la vista
            toastMessageLiveData.postValue(R.string.added_to_playlist);
        });
    }

    // meotodo para el botón "Crear nueva playlist" (Usa el String del usuario)
    public void createNewPlaylistWithSong(String playlistName, Song song) {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) return;

        executorService.execute(() -> {
            PlaylistEntity newPlaylist = new PlaylistEntity();
            newPlaylist.name = playlistName;
            newPlaylist.userId = currentUserId;
            newPlaylist.isFavorites = false;

            // Se inserta la playlist y captura la ID autogenerada
            int autogeneratedPlaylistId = (int) playlistDao.insertPlaylist(newPlaylist);

            TrackEntity trackEntity = new TrackEntity();
            trackEntity.deezerId = song.getId();
            trackEntity.title = song.getTitulo();
            trackEntity.artistName = song.getNameArtist();
            trackEntity.coverUrl = song.getUrlPortada();

            playlistDao.insertTrack(trackEntity);

            PlaylistTrackCrossRef crossRef = new PlaylistTrackCrossRef();
            crossRef.playlistId = autogeneratedPlaylistId; // Usa la ID nueva
            crossRef.deezerId = song.getId();

            playlistDao.insertTrackIntoPlaylist(crossRef);
        });
    }

    public void createNewEmptyPlaylist(String playlistName) {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) return;

        executorService.execute(() -> {
            PlaylistEntity newPlaylist = new PlaylistEntity();
            newPlaylist.name = playlistName;
            newPlaylist.userId = currentUserId;
            newPlaylist.isFavorites = false;

            // Solo insertamos la playlist en la base de datos
            playlistDao.insertPlaylist(newPlaylist);

            loadUserPlaylists();
        });
    }

    //  Canal de salida exclusivo para la vista PlayListFragment
    private MutableLiveData<List<Song>> currentPlaylistTracksLiveData = new MutableLiveData<>();

    public MutableLiveData<List<Song>> getCurrentPlaylistTracksLiveData() {
        return currentPlaylistTracksLiveData;
    }

    // metodo de búsqueda parametrizado
    public void searchInSpecificPlaylist(int playlistId, String newText) {
        executorService.execute(() -> {
            // Ejecución de la consulta genérica por ID
            List<TrackEntity> searchResults = playlistDao.searchInPlaylist(playlistId, newText);

            List<Song> songsForTheView = new ArrayList<>();

            // Mapeo de entidades a objetos de dominio
            if (searchResults != null) {
                for (TrackEntity entity : searchResults) {
                    Song songMap = new Song();
                    songMap.setId(entity.deezerId);
                    songMap.setTitulo(entity.title);

                    Song.Artist artistMap = new Song.Artist();
                    artistMap.name = entity.artistName;
                    songMap.setArtist(artistMap);

                    Song.Album albumMap = new Song.Album();
                    albumMap.coverUrl = entity.coverUrl;
                    songMap.setAlbum(albumMap);

                    songsForTheView.add(songMap);
                }
            }

            // Publicación en el canal correspondiente
            currentPlaylistTracksLiveData.postValue(songsForTheView);
        });
    }

    private MutableLiveData<List<Song>> songsPlaylistLiveData = new MutableLiveData<>();

    //canal para el nombre dinamico de la playlist
    private MutableLiveData<PlaylistEntity> currentPlaylistInfoLiveData = new MutableLiveData<>();

    public MutableLiveData<List<Song>> getSongsPlaylistLiveData() {
        return songsPlaylistLiveData;
    }

    public MutableLiveData<PlaylistEntity> getCurrentPlaylistInfoLiveData() {
        return currentPlaylistInfoLiveData;
    }

    public void loadPlaylist(int playlistId) {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) {
            errorLiveData.postValue(R.string.err_user_not_found);
            return;
        }

        executorService.execute(() -> {
            PlaylistWithTracks playlist = playlistDao.getPlaylistWithTracks(playlistId);
            List<Song> songsForTheView = new ArrayList<>();

            if (playlist != null && playlist.tracks != null) {
                for (TrackEntity entity : playlist.tracks) {
                    Song songMap = new Song();
                    songMap.setId(entity.deezerId);
                    songMap.setTitulo(entity.title);

                    Song.Artist artistMap = new Song.Artist();
                    artistMap.name = entity.artistName;
                    songMap.setArtist(artistMap);

                    Song.Album albumMap = new Song.Album();
                    albumMap.coverUrl = entity.coverUrl;
                    songMap.setAlbum(albumMap);

                    songsForTheView.add(songMap);
                }
                songsPlaylistLiveData.postValue(songsForTheView);
                currentPlaylistInfoLiveData.postValue(playlist.playlist);
            }
        });
    }

    //  Eliminar canción de la playlist activa
    public void removeSongFromPlaylist(int playlistId, long songId) {
        executorService.execute(() -> {
            // Ejecución de eliminación relacional
            playlistDao.removeTrackFromPlaylist(playlistId, songId);

            // Sincronización de UI: Recarga la playlist para desaparecer la fila visualmente
            loadPlaylist(playlistId);

            toastMessageLiveData.postValue(R.string.removed_from_playlist);
        });
    }

    // Eliminar playlist completa
    public void deleteEntirePlaylist(int playlistId) {
        executorService.execute(() -> {
            // 1. Prevención de huérfanos: Borrado de la tabla cruzada
            playlistDao.clearAllTracksFromPlaylist(playlistId);

            // 2. Borrado de la entidad principal
            playlistDao.deletePlaylist(playlistId);

            // Sincronización de UI: Recarga la biblioteca para quitar la portada
            loadUserPlaylists();
        });
    }


    private MutableLiveData<List<PlaylistWithTracks>> searchPlaylistLiveData = new MutableLiveData<>();

    public LiveData<List<PlaylistWithTracks>> getSearchPlaylistLiveData() {
        return searchPlaylistLiveData;
    }

    public void searchPlaylist(String name) {
        executorService.execute(() -> {
            // Pedimos a Room la playlist CON todas sus canciones para poder leer la portada
            List<PlaylistWithTracks> playlistsCompletas = playlistDao.searchPlaylistsWithTracks(name);

            // Enviamos al canal
            searchPlaylistLiveData.postValue(playlistsCompletas);
        });
    }


    private List<Song> manualQueue = new java.util.ArrayList<>();
    // metodo para encolar (FIFO)
    public void addToQueue(Song song) {
            // Se añade al final de la cola separada
            manualQueue.add(song);
            // Se notifica a la interfaz instantáneamente
            toastMessageLiveData.setValue(R.string.added_to_queue);
    }

}

