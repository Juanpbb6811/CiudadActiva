package com.ciudadactiva.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerificarCodigoRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    @Size(min = 6, max = 6, message = "El código de verificación debe contener 6 dígitos")
    private String codigo;

    public VerificarCodigoRequest() {
    }

    public VerificarCodigoRequest(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
