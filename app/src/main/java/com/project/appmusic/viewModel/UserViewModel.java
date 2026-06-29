package com.project.appmusic.viewModel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.project.appmusic.objetos.User;
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

    private MutableLiveData<User> currentUser = new MutableLiveData<>();

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

    public MutableLiveData<Boolean> getSuccess() {
        return success;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
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
            String example = getApplication().getString(R.string.your_email_address);
            errorMessage.setValue(error + example);
            return;
        }

        if (newUser.getPassword().length() < 8) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_long));
            return;
        }

        Pattern passPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9])\\S+$");
        Matcher matcher = passPattern.matcher(newUser.getPassword());
        if (!matcher.matches()) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_format));
            return;
        }
        // --- FIN DE VALIDACIONES ---


        executorService.execute(() -> {

            int emailCount = userDao.checkEmailExists(newUser.getEmail());

            if (emailCount > 0) {

                errorMessage.postValue(getApplication().getString(R.string.err_email_exists));
            } else {

                UserEntity newEntity = new UserEntity();
                newEntity.setUsername(newUser.getName());
                newEntity.setEmail(newUser.getEmail());

                String hashSeguro = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt());
                newEntity.setPassword(hashSeguro);

                userDao.insertUser(newEntity);
                success.postValue(true);
            }
        });

    }

    public void login(String email, String password, boolean rememberMe) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.err_empty_fields));
            return;
        }

        executorService.execute(() -> {
            UserEntity userEntity = userDao.findByEmail(email);

            if (userEntity == null) {
                errorMessage.postValue(getApplication().getString(R.string.err_user_not_found));
            } else {
                if (BCrypt.checkpw(password, userEntity.getPassword())) {

                    SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putInt("currentUserId", userEntity.getId());

                    if (rememberMe) {
                        editor.putBoolean("isLogged", true);
                    } else {
                        editor.putBoolean("isLogged", false);
                    }

                    editor.apply();

                    success.postValue(true);
                } else {
                    errorMessage.postValue(getApplication().getString(R.string.err_wrong_password));
                }
            }
        });
    }

    public void loadCurrentUser() {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);
        if (currentUserId == -1) {
            return;
        }
        executorService.execute(() -> {
            UserEntity userEntity = userDao.getUserById(currentUserId);

            if (userEntity != null) {
                User userModel = new User();
                userModel.setId(userEntity.getId());
                userModel.setName(userEntity.getUsername());
                userModel.setEmail(userEntity.getEmail());
                userModel.setProfilePicture(userEntity.getProfilePicture());

                currentUser.postValue(userModel);
            }
        });
    }

    public void changePassword(String oldPassword, String newPassword) {

        if (newPassword.length() < 8) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_long));
            return;
        }

        Pattern passPattern = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9])\\S+$");
        Matcher matcher = passPattern.matcher(newPassword);
        if (!matcher.matches()) {
            errorMessage.setValue(getApplication().getString(R.string.err_pass_format));
            return;
        }

        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);
        if (currentUserId == -1) {
            errorMessage.setValue(getApplication().getString(R.string.err_user_not_found));
            return;
        }

        executorService.execute(() -> {
            UserEntity userEntity = userDao.getUserById(currentUserId);

            if (userEntity != null) {
                if (BCrypt.checkpw(oldPassword, userEntity.getPassword())) {
                    String hashSeguro = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    userEntity.setPassword(hashSeguro);

                    userDao.updateUser(userEntity);

                    success.postValue(true);
                } else {
                    errorMessage.postValue(getApplication().getString(R.string.err_wrong_password));
                }
            }
        });
    }

    public void deleteCurrentAccount() {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId != -1) {
            executorService.execute(() -> {
                userDao.deleteUserById(currentUserId);

                prefs.edit()
                        .remove("currentUserId")
                        .remove("isLogged")
                        .apply();

                isLoggedOut.postValue(true);
            });
        }
    }

    private final MutableLiveData<Boolean> isLoggedOut = new MutableLiveData<>(false);

    public LiveData<Boolean> getIsLoggedOut() {
        return isLoggedOut;
    }

    public void logOut() {
        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("currentUserId")
                .remove("isLogged")
                .apply();

        isLoggedOut.setValue(true);
    }

    public void changeUserName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.err_empty_fields));
            return;
        }

        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) {
            errorMessage.setValue(getApplication().getString(R.string.err_user_not_found));
            return;
        }

        executorService.execute(() -> {
            UserEntity userEntity = userDao.getUserById(currentUserId);

            if (userEntity != null) {
                userEntity.setUsername(newName.trim());

                userDao.updateUser(userEntity);

                loadCurrentUser();

                success.postValue(true);
            }
        });
    }

    public void updateProfilePicture(Bitmap bitmap) {
        if (bitmap == null) return;

        SharedPreferences prefs = getApplication().getSharedPreferences("AppMusicPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("currentUserId", -1);

        if (currentUserId == -1) {
            errorMessage.setValue(getApplication().getString(R.string.err_user_not_found));
            return;
        }

        executorService.execute(() -> {
            // Convertir el Bitmap a un ByteArray
            byte[] imageBytes = compressBitmapToByteArray(bitmap);

            UserEntity userEntity = userDao.getUserById(currentUserId);

            if (userEntity != null) {
                userEntity.setProfilePicture(imageBytes);
                userDao.updateUser(userEntity);

                loadCurrentUser();

                success.postValue(true);
            }
        });
    }

    public void updateProfilePictureFromUri(android.net.Uri uri, Context context) {
        if (uri == null) return;

        executorService.execute(() -> {
            try {
                // Abrimos un flujo de lectura desde el almacenamiento del dispositivo
                java.io.InputStream imageStream = context.getContentResolver().openInputStream(uri);

                // Decodificamos el archivo y lo transformamos en un mapa de bits
                android.graphics.Bitmap selectedImage = android.graphics.BitmapFactory.decodeStream(imageStream);

                if (selectedImage != null) {
                    // Reutilizacion del metodo
                    updateProfilePicture(selectedImage);
                }
            } catch (java.io.FileNotFoundException e) {
                e.printStackTrace();
                errorMessage.postValue("Error al leer la imagen de la galería");
            }
        });
    }

    //metodo auxiliar
    private byte[] compressBitmapToByteArray(android.graphics.Bitmap originalBitmap) {
        // redimension de la imagen a un máximo de 800x800 píxeles para mantener la proporción
        int maxSize = 800;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true);

        // Comprimir el mapa de bits escalado a un arreglo de bytes en formato JPEG
        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, stream);

        return stream.toByteArray();
    }

}