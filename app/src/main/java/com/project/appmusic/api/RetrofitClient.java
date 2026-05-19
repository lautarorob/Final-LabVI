package com.project.appmusic.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//clase para crear el cliente
public class RetrofitClient {
    private static final String BASE_URL = "https://api.deezer.com/";
    private static Retrofit retrofit = null;

    public static DeezerApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(DeezerApiService.class);
    }
}
