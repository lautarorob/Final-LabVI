package com.project.appmusic.playlistCreation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.project.appmusic.R;
import com.project.appmusic.Song;
import com.project.appmusic.viewModel.MusicViewModel;

public class CreatePlaylistFragment extends Fragment {


    public CreatePlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_playlist, container, false);
    }

    private MusicViewModel musicViewModel;
    private Song songSelect;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        com.google.android.material.button.MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);
        com.google.android.material.button.MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        EditText etPlaylistName = view.findViewById(R.id.etPlaylistName);


        btnCancel.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        btnConfirm.setOnClickListener(v -> {
            String nameExtracted = etPlaylistName.getText().toString().trim();

            if (!nameExtracted.isEmpty() && songSelect != null) {
                musicViewModel.createNewPlaylistWithSong(nameExtracted, songSelect);
                getParentFragmentManager().popBackStack();
            }
        });
    }

    public void setSong(Song songSelect) {
        this.songSelect = songSelect;
    }
}
