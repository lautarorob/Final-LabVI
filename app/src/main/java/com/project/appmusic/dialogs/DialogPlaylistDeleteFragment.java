package com.project.appmusic.dialogs;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;


import com.project.appmusic.R;

import com.project.appmusic.viewModel.MusicViewModel;


public class DialogPlaylistDeleteFragment extends DialogFragment {


    private int playlistId;
    private String playlistName;
    private MusicViewModel musicViewModel;


    public DialogPlaylistDeleteFragment(int playlistId, String playlistName) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
    }


    @Nullable

    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_dialog_playlist_delete, container, false);
    }


    @Override

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        TextView tvDeleteMessage = view.findViewById(R.id.tvDeleteMessage);
        TextView btnCancel = view.findViewById(R.id.btnCancelDelete);
        TextView btnConfirm = view.findViewById(R.id.btnConfirmDelete);

        tvDeleteMessage.setText(getString(R.string.are_you_sure_you_want_to_delete_this_playlist, playlistName));
        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            musicViewModel.deleteEntirePlaylist(playlistId);
            dismiss();

        });

    }



}