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
        return calcularPorCategoria(cantidad, null);
    }

    /**
     * Calcula la escala semáforo según el número de reportes acumulados y la categoría de la incidencia:
     * - Categorías de alto riesgo físico (Alumbrado, Señalización, Alcantarillado): tolerancias más estrictas.
     * - Categorías de malla vial: progresión según volumen de quejas en calzada.
     * - Categorías de aseo y parques: progresión según acumulación ambiental.
     */
    public static SemaforoDTO calcularPorCategoria(int cantidad, String categoriaNombre) {
        String cat = categoriaNombre != null ? categoriaNombre.toLowerCase() : "";

        // 1. Alto Riesgo Inmediato: Alumbrado / Riesgo Eléctrico / Semáforos / Alcantarillado abierto
        if (cat.contains("alumbrado") || cat.contains("luz") || cat.contains("poste") ||
            cat.contains("señal") || cat.contains("semaforo") || cat.contains("semáforo") ||
            cat.contains("alcantarill") || cat.contains("acueducto")) {

            if (cantidad <= 1) {
                return new SemaforoDTO(cantidad, "#8BC34A", "VERDE_LIMA", "MODERADO",
                        "1 reporte de seguridad/redes: Atención preventiva en sector", 30.0);
            } else if (cantidad <= 3) {
                return new SemaforoDTO(cantidad, "#FF9800", "NARANJA", "ALTO",
                        cantidad + " reportes de seguridad/redes acumulados: Intervención prioritaria", 75.0);
            } else {
                return new SemaforoDTO(cantidad, "#F44336", "ROJO", "CRITICO",
                        cantidad + " reportes críticos en la zona: Emergencia en infraestructura de riesgo", 100.0);
            }
        }

        // 2. Malla Vial y Pavimento (Baches, calzada, hundimientos)
        if (cat.contains("vía") || cat.contains("via") || cat.contains("pavimento") || cat.contains("calzada")) {
            if (cantidad <= 1) {
                return new SemaforoDTO(cantidad, "#4CAF50", "VERDE", "BAJO", "1 reporte vial: Bache o daño puntual aislado", 15.0);
            } else if (cantidad <= 4) {
                return new SemaforoDTO(cantidad, "#FFEB3B", "AMARILLO", "MEDIO", cantidad + " reportes viales: Deterioro vial moderado", 50.0);
            } else if (cantidad <= 7) {
                return new SemaforoDTO(cantidad, "#FF9800", "NARANJA", "ALTO", cantidad + " reportes viales: Afectación notable de tránsito", 75.0);
            } else {
                return new SemaforoDTO(cantidad, "#F44336", "ROJO", "CRITICO", cantidad + " reportes viales: Tramo crítico intransitable", 100.0);
            }
        }

        // 3. Basuras, Aseo y Parques
        if (cantidad <= 2) {
            return new SemaforoDTO(cantidad, "#4CAF50", "VERDE", "BAJO", cantidad + " reporte(s): Aseo y ornato en rango bajo", 20.0);
        } else if (cantidad <= 5) {
            return new SemaforoDTO(cantidad, "#FFEB3B", "AMARILLO", "MEDIO", cantidad + " reportes: Acumulación moderada en espacio público", 50.0);
        } else if (cantidad <= 8) {
            return new SemaforoDTO(cantidad, "#FF9800", "NARANJA", "ALTO", cantidad + " reportes: Foco de insalubridad o deterioro evidente", 80.0);
        } else {
            return new SemaforoDTO(cantidad, "#F44336", "ROJO", "CRITICO", cantidad + " reportes: Emergencia ambiental/sanitaria en el sector", 100.0);
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
