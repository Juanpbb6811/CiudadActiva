package com.ciudadactiva.dto.auth;

import com.ciudadactiva.entity.TipoDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    private String primerNombre;
    private String segundoNombre;
    private String primerApellido;
    private String segundoApellido;

    private TipoDocumento tipoDocumento = TipoDocumento.CC;

    private String numeroDocumento;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    private String telefono;

    private String direccion;

    // Campo de compatibilidad
    private String nombre;

    public RegisterRequest() {
    }

    public RegisterRequest(String primerNombre, String segundoNombre, String primerApellido, String segundoApellido,
                           TipoDocumento tipoDocumento, String numeroDocumento,
                           String email, String password, String telefono, String direccion) {
        this.primerNombre = primerNombre;
        this.segundoNombre = segundoNombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.tipoDocumento = tipoDocumento != null ? tipoDocumento : TipoDocumento.CC;
        this.numeroDocumento = numeroDocumento;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.direccion = direccion;
        this.nombre = armarNombreCompleto();
    }

    public String armarNombreCompleto() {
        if (primerNombre != null && !primerNombre.isBlank()) {
            StringBuilder sb = new StringBuilder(primerNombre.trim());
            if (segundoNombre != null && !segundoNombre.isBlank()) sb.append(" ").append(segundoNombre.trim());
            if (primerApellido != null && !primerApellido.isBlank()) sb.append(" ").append(primerApellido.trim());
            if (segundoApellido != null && !segundoApellido.isBlank()) sb.append(" ").append(segundoApellido.trim());
            return sb.toString();
        }
        return nombre != null && !nombre.isBlank() ? nombre : "Ciudadano";
    }

    public String getPrimerNombre() {
        return primerNombre;
    }

    public void setPrimerNombre(String primerNombre) {
        this.primerNombre = primerNombre;
    }

    public String getSegundoNombre() {
        return segundoNombre;
    }

    public void setSegundoNombre(String segundoNombre) {
        this.segundoNombre = segundoNombre;
    }

    public String getPrimerApellido() {
        return primerApellido;
    }

    public void setPrimerApellido(String primerApellido) {
        this.primerApellido = primerApellido;
    }

    public String getSegundoApellido() {
        return segundoApellido;
    }

    public void setSegundoApellido(String segundoApellido) {
        this.segundoApellido = segundoApellido;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNombre() {
        if (nombre != null && !nombre.isBlank()) {
            return nombre;
        }
        return armarNombreCompleto();
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
