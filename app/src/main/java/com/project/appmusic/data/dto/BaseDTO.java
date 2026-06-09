package com.project.appmusic.data.dto;

import java.io.Serializable;

public abstract class BaseDTO implements Serializable {

    // Atributos de auditoría o trazabilidad comunes a TODAS las peticiones
    private final long timestampCreacion;

    public BaseDTO() {
        // Registra automáticamente el momento exacto en que se empaquetaron los datos
        this.timestampCreacion = System.currentTimeMillis();
    }

    public long getTimestampCreacion() {
        return timestampCreacion;
    }

    // Metodo genérico para facilitar la depuración (Logs) en la consola
    @Override
    public String toString() {
        return "BaseDTO{" +
                "timestampCreacion=" + timestampCreacion +
                '}';
    }
}