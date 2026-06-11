package com.project.appmusic.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.project.appmusic.MainActivity;
import com.project.appmusic.R;
import com.project.appmusic.viewModel.UserViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView txtCreateAccount;
    private MaterialCheckBox cbRememberMe; // 1. Declarar el CheckBox

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Se ejecuta ANTES de dibujar la pantalla
        SharedPreferences prefs = getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        if (prefs.getBoolean("isLogged", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return; // Detiene el onCreate para no cargar la interfaz de login
        }
        // ------------------------------------------

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        this.etEmail = findViewById(R.id.etEmail);
        this.etPassword = findViewById(R.id.etPassword);
        this.btnLogin = findViewById(R.id.btnLogin);
        this.txtCreateAccount = findViewById(R.id.txtCreateAccount);
        this.cbRememberMe = findViewById(R.id.cbRememberMe); // 2. Vincular vista

        this.userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        txtCreateAccount.setOnClickListener(view -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateAccountFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnLogin.setOnClickListener(view -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            boolean rememberMe = cbRememberMe.isChecked(); // 3. Leer estado del checkbox

            // 4. Enviar el parámetro adicional al ViewModel
            userViewModel.login(email, password, rememberMe);
        });

        // Observadores
        userViewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getSuccess().observe(this, success -> {
            if (success != null && success) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}