package com.project.appmusic.playbackFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar; // Actualizado de ProgressBar a SeekBar
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.project.appmusic.MainActivity;
import com.project.appmusic.R;
import com.project.appmusic.objetos.Song;
import com.project.appmusic.viewModel.MusicViewModel;

import java.util.Locale;

public class FullPlayerFragment extends Fragment {

    private MusicViewModel musicViewModel;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout rootLayout = view.findViewById(R.id.player_root);
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        ImageView btnFavorite = view.findViewById(R.id.btn_favorite);
        ImageView btnArrowDown = view.findViewById(R.id.arrow_down);

        // Vinculación de los componentes visuales
        SeekBar seekBar = view.findViewById(R.id.seekBar_progress);
        TextView tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        TextView tvTotalTime = view.findViewById(R.id.tvTotalTime);

        // datos de la cancion
        TextView txtSongTitle = view.findViewById(R.id.songTitle);
        TextView txtArtistName = view.findViewById(R.id.artistName);
        ImageView imgCover = view.findViewById(R.id.albumCover);

        // control de reproduccion
        ImageView btnPlayPause = view.findViewById(R.id.playPause);
        ImageView btnShuffle = view.findViewById(R.id.shuffle);
        ImageView btnPrevious = view.findViewById(R.id.skipPrevious);
        ImageView btnNext = view.findViewById(R.id.skipNext);
        ImageView btnRepeat = view.findViewById(R.id.repeat);

        // observador de reproduccion
        musicViewModel.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            if (isPlaying != null && isPlaying) {
                btnPlayPause.setImageResource(R.drawable.ic_pause_circle);
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play_circle);
            }
        });

        // observador de datos de la cancion
        musicViewModel.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            if (song != null) {
                txtSongTitle.setText(song.getTitulo());
                txtArtistName.setText(song.getNameArtist());

                // glide carga la imagen como bitmap
                Glide.with(this)
                        .asBitmap()
                        .load(song.getUrlPortada())
                        .into(new com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {

                            @Override
                            public void onResourceReady(@NonNull android.graphics.Bitmap bitmap, @Nullable com.bumptech.glide.request.transition.Transition<? super android.graphics.Bitmap> transition) {
                                imgCover.setImageBitmap(bitmap);

                                androidx.palette.graphics.Palette.from(bitmap).generate(palette -> {
                                    if (palette != null) {
                                        int dominantColor = palette.getDarkMutedColor(android.graphics.Color.parseColor("#121212"));
                                        android.graphics.drawable.GradientDrawable gradient = new android.graphics.drawable.GradientDrawable(
                                                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                                                new int[]{dominantColor, android.graphics.Color.parseColor("#000000")}
                                        );
                                        gradient.setCornerRadius(0f);
                                        rootLayout.setBackground(gradient);
                                    }
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable android.graphics.drawable.Drawable placeholder) {
                            }
                        });
            }
        });

        btnArrowDown.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnPlayPause.setOnClickListener(v -> musicViewModel.togglePlayback());
        btnNext.setOnClickListener(v -> musicViewModel.playNextSong(true));
        btnPrevious.setOnClickListener(v -> musicViewModel.playPreviousSong());
        btnShuffle.setOnClickListener(v -> musicViewModel.toggleShuffle());
        btnRepeat.setOnClickListener(v -> musicViewModel.toggleRepeat());

        musicViewModel.getIsCurrentFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
            if (isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite);
            }
        });

        btnFavorite.setOnClickListener(v -> {
            Song currentSong = musicViewModel.getCurrentSong().getValue();
            if (currentSong != null) {
                musicViewModel.toggleFavorite(currentSong);
            }
        });

        musicViewModel.getIsShuffle().observe(getViewLifecycleOwner(), isShuffle -> {
            if (isShuffle != null && isShuffle) {
                btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
                btnShuffle.setColorFilter(android.graphics.Color.parseColor("#1D74FF"));
            } else {
                btnShuffle.setImageResource(R.drawable.ic_shuffle);
                btnShuffle.setColorFilter(android.graphics.Color.parseColor("#FFFFFF"));
            }
        });

        musicViewModel.getRepeatMode().observe(getViewLifecycleOwner(), isRepeat -> {
            switch (isRepeat) {
                case MusicViewModel.REPEAT_MODE_OFF:
                    btnRepeat.setImageResource(R.drawable.ic_repeat);
                    btnRepeat.setColorFilter(android.graphics.Color.parseColor("#FFFFFF"));
                    break;
                case MusicViewModel.REPEAT_MODE_ALL:
                    btnRepeat.setImageResource(R.drawable.ic_repeat_on);
                    btnRepeat.setColorFilter(android.graphics.Color.parseColor("#1D74FF"));
                    break;
                case MusicViewModel.REPEAT_MODE_ONE:
                    btnRepeat.setImageResource(R.drawable.ic_repeat_one_on);
                    btnRepeat.setColorFilter(android.graphics.Color.parseColor("#1D74FF"));
                    break;
            }
        });

        //  Control de eventos táctiles en la barra de progreso
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Actualización visual del reloj izquierdo mientras se arrastra
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Detener el cronómetro interno
                handler.removeCallbacks(progressRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Ejecutar el salto de memoria en el motor de audio
                musicViewModel.seekToPosition(seekBar.getProgress());
                // Reanudar el cronómetro visual
                handler.post(progressRunnable);
            }
        });

        //  Hilo de actualización periódica
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                long currentPosition = musicViewModel.getCurrentPosition();
                long totalDuration = musicViewModel.getDuration();

                if (totalDuration > 0) {
                    seekBar.setMax((int) totalDuration);
                    seekBar.setProgress((int) currentPosition);

                    // Actualización estricta de relojes
                    tvCurrentTime.setText(formatTime((int) currentPosition));
                    tvTotalTime.setText(formatTime((int) totalDuration));
                }
                // Tasa de refresco: 1 segundo (1000ms)
                handler.postDelayed(this, 1000);
            }
        };
    }

    // metodo matemático auxiliar para formateo de tiempo
    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) requireActivity()).setMiniPlayerVisibility(false);
        handler.post(progressRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((MainActivity) requireActivity()).setMiniPlayerVisibility(true);
        handler.removeCallbacks(progressRunnable);
    }
}