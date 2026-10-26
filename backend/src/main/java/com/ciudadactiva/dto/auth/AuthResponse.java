package com.ciudadactiva.dto.auth;

import com.ciudadactiva.dto.usuario.UsuarioResponse;

public class AuthResponse {

    private String token;
    private String tipoToken = "Bearer";
    private UsuarioResponse usuario;

    public AuthResponse() {
    }

    public AuthResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.tipoToken = "Bearer";
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public void setTipoToken(String tipoToken) {
        this.tipoToken = tipoToken;
    }

    public UsuarioResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioResponse usuario) {
        this.usuario = usuario;
    }
}
