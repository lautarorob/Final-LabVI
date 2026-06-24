package com.project.appmusic.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.project.appmusic.R;
import com.project.appmusic.data.dto.UserRegistrationDTO;
import com.project.appmusic.viewModel.UserViewModel;


public class CreateAccountFragment extends Fragment {

    private EditText etname;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;

    private UserViewModel userViewModel;

    private com.google.android.material.button.MaterialButton btnCreateAccount;


    public CreateAccountFragment() {
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
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etname = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etRegisterEmail);
        etPassword = view.findViewById(R.id.etRegisterPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);


        TextView txtLogin = view.findViewById(R.id.txtGoToLogin);
        txtLogin.setOnClickListener(view1 -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });


        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        configureObservers();

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        userViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.account_created_successfully, Toast.LENGTH_SHORT).show();
                //se destruye el fragmento
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            saveUser();
        });


    }

    private void configureObservers() {
        //  fallos de validación o de inserción
        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        //   éxito de la persistencia asíncrona
        userViewModel.getSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), getString(R.string.msg_registration_success), Toast.LENGTH_SHORT).show();
                // Cierra el fragmento
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void saveUser() {
        String name = etname.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        UserRegistrationDTO registrationData = new UserRegistrationDTO(name, email, password, confirmPassword);

        userViewModel.saveUser(registrationData);
    }
}