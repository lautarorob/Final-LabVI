package com.project.appmusic.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.project.appmusic.R;
import com.project.appmusic.viewModel.MusicViewModel;
import com.project.appmusic.viewModel.UserViewModel;

public class DialogLogoutFragment extends DialogFragment {

    private MusicViewModel musicViewModel;
    private UserViewModel userViewModel;

    public DialogLogoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fondo transparente para respetar los bordes redondeados del XML
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_dialog_logout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);

        TextView btnCancel = view.findViewById(R.id.btnCancelDelete);
        TextView btnConfirm = view.findViewById(R.id.btnConfirmDelete);

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            if (musicViewModel != null) {
                musicViewModel.getIsPlaying().setValue(false);
            }

            android.widget.Toast.makeText(requireContext(), R.string.logged_out, android.widget.Toast.LENGTH_SHORT).show();

            if (userViewModel != null) {
                userViewModel.logOut();
            }

            dismiss();
        });
    }
}