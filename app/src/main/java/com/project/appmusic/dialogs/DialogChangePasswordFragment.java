package com.project.appmusic.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.project.appmusic.R;
import com.project.appmusic.viewModel.UserViewModel;


public class DialogChangePasswordFragment extends DialogFragment {


    public DialogChangePasswordFragment() {
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
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_dialog_change_password, container, false);
    }

    private UserViewModel userViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnConfirm = view.findViewById(R.id.btnConfirm);
        com.google.android.material.textfield.TextInputEditText etOldPassword = view.findViewById(R.id.etOldPassword);
        com.google.android.material.textfield.TextInputEditText etNewPassword = view.findViewById(R.id.etNewPassword);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);


        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                userViewModel.getErrorMessage().postValue(null);
            }
        });

        userViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                android.widget.Toast.makeText(requireContext(), R.string.password_updated, android.widget.Toast.LENGTH_SHORT).show();
                userViewModel.getSuccess().postValue(null);
                dismiss();
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String oldPassword = etOldPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), getString(R.string.err_empty_fields), android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                android.widget.Toast.makeText(requireContext(), getString(R.string.err_pass_no_match), android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.changePassword(oldPassword, newPassword);
        });

        btnCancel.setOnClickListener(v -> {
            dismiss();
        });

    }
}