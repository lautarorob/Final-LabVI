package com.project.appmusic.data.converters;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class StringListConverters {

    // Traduce de Lista a String (Para GUARDAR en la base de datos)
    @TypeConverter
    public String fromStringList(List<String> list) {
        if (list == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    // Traduce de String a Lista (Para LEER de la base de datos)
    @TypeConverter
    public List<String> toStringList(String data) {
        if (data == null) {
            return null;
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(data, listType);
    }
}