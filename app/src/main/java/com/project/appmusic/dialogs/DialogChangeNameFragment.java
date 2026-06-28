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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.project.appmusic.R;
import com.project.appmusic.viewModel.UserViewModel;

public class DialogChangeNameFragment extends DialogFragment {

    private UserViewModel userViewModel;
    private EditText etNewName;

    public DialogChangeNameFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.fragment_dialog_change_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnConfirm = view.findViewById(R.id.btnConfirm);
        etNewName = view.findViewById(R.id.etNewName);

        //  Pre-cargar el nombre actual en el campo de texto
        if (userViewModel.getCurrentUser().getValue() != null) {
            etNewName.setText(userViewModel.getCurrentUser().getValue().getName());
        }

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                userViewModel.getErrorMessage().postValue(null);
            }
        });

        userViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), R.string.name_changed, Toast.LENGTH_SHORT).show();
                userViewModel.getSuccess().postValue(null);
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            String newName = etNewName.getText().toString();
            if (newName.trim().isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.err_empty_fields), Toast.LENGTH_SHORT).show();
                return;
            }

            userViewModel.changeUserName(newName);
        });
    }
}