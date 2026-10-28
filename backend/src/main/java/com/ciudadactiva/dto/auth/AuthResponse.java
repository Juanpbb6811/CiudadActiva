package com.ciudadactiva.dto.auth;

import com.ciudadactiva.dto.usuario.UsuarioResponse;

public class AuthResponse {

    private String token;
    private String tipoToken = "Bearer";
    private UsuarioResponse usuario;
    private Boolean requiereVerificacion = false;
    private String codigoGeneradoDemo;

    public AuthResponse() {
    }

    public AuthResponse(String token, UsuarioResponse usuario) {
        this.token = token;
        this.tipoToken = "Bearer";
        this.usuario = usuario;
    }

    public AuthResponse(String token, UsuarioResponse usuario, Boolean requiereVerificacion, String codigoGeneradoDemo) {
        this.token = token;
        this.tipoToken = "Bearer";
        this.usuario = usuario;
        this.requiereVerificacion = requiereVerificacion;
        this.codigoGeneradoDemo = codigoGeneradoDemo;
    }

    public Boolean getRequiereVerificacion() {
        return requiereVerificacion;
    }

    public void setRequiereVerificacion(Boolean requiereVerificacion) {
        this.requiereVerificacion = requiereVerificacion;
    }

    public String getCodigoGeneradoDemo() {
        return codigoGeneradoDemo;
    }

    public void setCodigoGeneradoDemo(String codigoGeneradoDemo) {
        this.codigoGeneradoDemo = codigoGeneradoDemo;
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
