package com.ciudadactiva.dto.common;

public class SemaforoDTO {

    private int cantidadReportes;
    private String colorHex;
    private String colorNombre;
    private String nivelAlerta;
    private String descripcion;
    private double porcentajeSaturacion;

    public SemaforoDTO() {
    }

    public SemaforoDTO(int cantidadReportes, String colorHex, String colorNombre, String nivelAlerta, String descripcion, double porcentajeSaturacion) {
        this.cantidadReportes = cantidadReportes;
        this.colorHex = colorHex;
        this.colorNombre = colorNombre;
        this.nivelAlerta = nivelAlerta;
        this.descripcion = descripcion;
        this.porcentajeSaturacion = porcentajeSaturacion;
    }

    /**
     * Calcula la escala semáforo según el número de reportes:
     * - 0 reportes: Sin incidencias (#4CAF50 Verde)
     * - 1-2 reportes: Verde (#4CAF50 - Bajo / Controlado)
     * - 3-4 reportes: Verde Claro / Lima (#8BC34A - Leve)
     * - 5-6 reportes: Amarillo (#FFEB3B - Moderado / Alerta Preventiva)
     * - 7-8 reportes: Naranja (#FF9800 - Alto / Atención Urgente)
     * - 9-10+ reportes: Rojo (#F44336 - Crítico / Emergencia)
     */
    public static SemaforoDTO calcular(int cantidad) {
        if (cantidad <= 0) {
            return new SemaforoDTO(0, "#4CAF50", "VERDE", "SIN_REPORTES", "Sin incidencias reportadas en el sector", 0.0);
        } else if (cantidad <= 2) {
            return new SemaforoDTO(cantidad, "#4CAF50", "VERDE", "BAJO", "1 a 2 reportes: Nivel bajo, situación bajo control", Math.min(100.0, cantidad * 10.0));
        } else if (cantidad <= 4) {
            return new SemaforoDTO(cantidad, "#8BC34A", "VERDE_LIMA", "LEVE", "3 a 4 reportes: Nivel leve en observación", cantidad * 10.0);
        } else if (cantidad <= 6) {
            return new SemaforoDTO(cantidad, "#FFEB3B", "AMARILLO", "MODERADO", "5 a 6 reportes: Nivel moderado, alerta preventiva", cantidad * 10.0);
        } else if (cantidad <= 8) {
            return new SemaforoDTO(cantidad, "#FF9800", "NARANJA", "ALTO", "7 a 8 reportes: Nivel alto, requiere atención prioritaria", cantidad * 10.0);
        } else {
            return new SemaforoDTO(cantidad, "#F44336", "ROJO", "CRITICO", "10 o más reportes: Nivel rojo crítico, máxima urgencia operativa", 100.0);
        }
    }

    public int getCantidadReportes() {
        return cantidadReportes;
    }

    public void setCantidadReportes(int cantidadReportes) {
        this.cantidadReportes = cantidadReportes;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getColorNombre() {
        return colorNombre;
    }

    public void setColorNombre(String colorNombre) {
        this.colorNombre = colorNombre;
    }

    public String getNivelAlerta() {
        return nivelAlerta;
    }

    public void setNivelAlerta(String nivelAlerta) {
        this.nivelAlerta = nivelAlerta;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPorcentajeSaturacion() {
        return porcentajeSaturacion;
    }

    public void setPorcentajeSaturacion(double porcentajeSaturacion) {
        this.porcentajeSaturacion = porcentajeSaturacion;
    }
}
