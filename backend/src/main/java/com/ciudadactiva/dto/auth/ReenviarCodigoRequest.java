package com.ciudadactiva.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ReenviarCodigoRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String email;

    private String tipo = "VERIFICACION"; // VERIFICACION o RECUPERACION

    public ReenviarCodigoRequest() {
    }

    public ReenviarCodigoRequest(String email, String tipo) {
        this.email = email;
        this.tipo = tipo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
