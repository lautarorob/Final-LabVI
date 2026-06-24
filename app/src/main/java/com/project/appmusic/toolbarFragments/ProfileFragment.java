package com.project.appmusic.toolbarFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.project.appmusic.R;
import com.project.appmusic.data.entity.PlaylistWithTracks;
import com.project.appmusic.dialogs.DialogChangeNameFragment;
import com.project.appmusic.dialogs.DialogChangePasswordFragment;
import com.project.appmusic.dialogs.DialogUserDeleteFragment;
import com.project.appmusic.likedSongsFragments.LikedSongsFragment;
import com.project.appmusic.login.LoginActivity;
import com.project.appmusic.viewModel.MusicViewModel;
import com.project.appmusic.viewModel.UserViewModel;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    private MusicViewModel musicViewModel;
    private UserViewModel userViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicViewModel = new ViewModelProvider(requireActivity()).get(MusicViewModel.class);
        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        userViewModel.loadCurrentUser();

        ImageView ivProfile = view.findViewById(R.id.ivProfile);
        TextView tvNameUser = view.findViewById(R.id.tvNameUser);
        TextView tvEmailUser = view.findViewById(R.id.tvEmailUser);
        LinearLayout layoutLikes = view.findViewById(R.id.layoutLikes);
        LinearLayout layoutPlaylists = view.findViewById(R.id.layoutPlaylists);
        LinearLayout tvChangePassword = view.findViewById(R.id.layoutChangePassword);
        com.google.android.material.button.MaterialButton btnLogOut = view.findViewById(R.id.btnLogOut);
        com.google.android.material.button.MaterialButton btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        ImageView ivEditName = view.findViewById(R.id.ivEditName);

        userViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvNameUser.setText(user.getName());
                tvEmailUser.setText(user.getEmail());

                // Extraer los bytes de la imagen
                byte[] imageBytes = user.getProfilePicture();

                if (imageBytes != null && imageBytes.length > 0) {
                    //  Conversión de byte[] a Bitmap
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                    // Inyectar el Bitmap renderizado en el ImageView
                    ivProfile.setImageBitmap(bitmap);
                } else {
                    // Fallback: Si el usuario no tiene foto, mostrar una imagen por defecto
                    ivProfile.setImageResource(R.drawable.ic_profile);
                    ivProfile.setBackgroundResource(R.color.light_blue);
                }
            }
        });

        userViewModel.getIsLoggedOut().observe(getViewLifecycleOwner(), loggedOut -> {
            if (loggedOut) {
                android.content.Intent intent = new android.content.Intent(getActivity(), LoginActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        btnLogOut.setOnClickListener(v -> {
            // Detener la música
            if (musicViewModel != null) {
                musicViewModel.getIsPlaying().setValue(false);
            }
            // Delegar la responsabilidad al ViewModel
            android.widget.Toast.makeText(requireContext(), R.string.logged_out, android.widget.Toast.LENGTH_SHORT).show();
            userViewModel.logOut();
        });

        //se cargan los favoritos
        musicViewModel.loadFavorites();

        //se actualiza la cantidad de favoritos
        musicViewModel.getFavoritesLiveData().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                TextView tvLikesCount = view.findViewById(R.id.tvLikesCount);
                tvLikesCount.setText(String.valueOf(songs.size()));
            }
        });

        //se cargan las playlists
        musicViewModel.loadUserPlaylists();

        //se actualiza la cantidad de playlists
        musicViewModel.getUserPlaylistsLiveData().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                TextView tvPlaylistsCount = view.findViewById(R.id.tvPlaylistsCount);
                tvPlaylistsCount.setText(String.valueOf(playlists.size()));
            }
            if (playlists.size() == 0) {
                TextView tvPlaylistsCount = view.findViewById(R.id.tvPlaylistsCount);
                tvPlaylistsCount.setText(String.valueOf(1));
            }
        });

        layoutLikes.setOnClickListener(v -> {
            Fragment fragment = new LikedSongsFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fullscreen_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        layoutPlaylists.setOnClickListener(v -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    requireActivity().findViewById(R.id.bottomNavigation);

            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_library);
            }
        });

        tvChangePassword.setOnClickListener(v -> {
            DialogChangePasswordFragment dialog = new DialogChangePasswordFragment();
            dialog.show(getParentFragmentManager(), "DialogChangePassword");
        });


        btnDeleteAccount.setOnClickListener(v -> {
            DialogUserDeleteFragment dialog = new DialogUserDeleteFragment();
            dialog.show(getParentFragmentManager(), "DialogUserDelete");
        });

        ivEditName.setOnClickListener(v -> {
            DialogChangeNameFragment dialog = new DialogChangeNameFragment();
            dialog.show(getParentFragmentManager(), "DialogChangeName");
        });


    }
}