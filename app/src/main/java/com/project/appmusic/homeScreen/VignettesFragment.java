package com.project.appmusic.homeScreen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.appmusic.R;
import com.project.appmusic.likedSongsScreen.LikedScreen;
import com.project.appmusic.regionalScreen.RegionalScreen;

public class VignettesFragment extends Fragment {

    private TextView txtTitulo;
    private ImageView imgVignette;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vignettes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTitulo = view.findViewById(R.id.txt_titulo);
        imgVignette = view.findViewById(R.id.img_portada);

        String typeVignette = getTag();


        if (typeVignette != null && typeVignette.equals("Ilike")) {

            if (txtTitulo != null) txtTitulo.setText(R.string.tus_me_gusta);
            if (imgVignette != null) imgVignette.setImageResource(R.drawable.like_logo);
            view.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), LikedScreen.class);
                startActivity(intent);
            });

        } else {

            if (txtTitulo != null) txtTitulo.setText(R.string.regionales);
            if (imgVignette != null) imgVignette.setImageResource(R.drawable.regional_logo);
            view.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), RegionalScreen.class);
                startActivity(intent);
            });

        }
    }
}