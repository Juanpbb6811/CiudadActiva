package com.ciudadactiva.dto.auth;

import com.ciudadactiva.entity.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CompletarDatosLegalesRequest {

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(min = 2, max = 60, message = "El primer nombre debe tener entre 2 y 60 caracteres")
    private String primerNombre;

    private String segundoNombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(min = 2, max = 60, message = "El primer apellido debe tener entre 2 y 60 caracteres")
    private String primerApellido;

    @Size(max = 60, message = "El segundo apellido no puede exceder 60 caracteres")
    private String segundoApellido;

    @NotNull(message = "El tipo de documento es obligatorio")
    private TipoDocumento tipoDocumento = TipoDocumento.CC;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(min = 5, max = 20, message = "El número de documento debe tener entre 5 y 20 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "El número de teléfono móvil es obligatorio")
    @Size(min = 7, max = 20, message = "El número de teléfono debe ser válido")
    private String telefono;

    @NotBlank(message = "La dirección de residencia es obligatoria")
    @Size(min = 5, max = 150, message = "La dirección de residencia debe ser detallada")
    private String direccion;

    public CompletarDatosLegalesRequest() {
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
}
