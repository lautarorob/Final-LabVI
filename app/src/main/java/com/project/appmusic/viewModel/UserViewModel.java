package com.project.appmusic.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.appmusic.data.dto.UserRegistrationDTO;
import com.project.appmusic.R;
import com.project.appmusic.data.entity.UserEntity;
// IMPORTANTE: Asegúrate de importar tu base de datos y tu DAO
import com.project.appmusic.data.database.AppDatabase;
import com.project.appmusic.data.dao.UserDao;

import org.mindrot.jbcrypt.BCrypt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserViewModel extends AndroidViewModel {

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> success = new MutableLiveData<>();

    //Declaración de las dependencias de persistencia y concurrencia
    private final UserDao userDao;
    private final ExecutorService executorService;

    public UserViewModel(@NonNull Application application) {
        super(application);
        // Inicialización de las dependencias de persistencia y concurrencia
        AppDatabase db = AppDatabase.getInstance(application);
        this.userDao = db.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSuccess() {
        return success;
    }

    public void saveUser(UserRegistrationDTO newUser) {

        // --- INICIO DE VALIDACIONES ---
        if (newUser.getName().isEmpty() || newUser.getEmail().isEmpty() || newUser.getPassword().isEmpty() || newUser.getConfirmPassword().isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.err_empty_fields));
            return;
        }

        if (!newUser.getPassword().equals(newUser.getConfirmPassword())) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_no_match));
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newUser.getEmail()).matches()) {
            String error = getApplication().getString(R.string.err_email_format);
            String example = " (Ej: email@example.com)";
            errorMessage.setValue(error + example);
            return;
        }

        if (newUser.getPassword().length() < 8) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_long));
            return;
        }

        Pattern passPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$");
        Matcher matcher = passPattern.matcher(newUser.getPassword());
        if (!matcher.matches()) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_format));
            return;
        }
        // --- FIN DE VALIDACIONES ---

        // solo se instancia si los datos son válidos
        UserEntity newEntity = new UserEntity();
        newEntity.setUsername(newUser.getName());
        newEntity.setEmail(newUser.getEmail());

        String hashSeguro = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
        newEntity.setPassword(hashSeguro);

        // ejecución asíncrona única
        executorService.execute(() -> {
            userDao.insertUser(newEntity);
            success.postValue(true);
        });

    }
}