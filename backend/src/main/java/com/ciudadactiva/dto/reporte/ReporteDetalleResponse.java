package com.ciudadactiva.dto.reporte;

import com.ciudadactiva.dto.asignacion.AsignacionResponse;
import com.ciudadactiva.dto.comentario.ComentarioResponse;
import com.ciudadactiva.dto.seguimiento.SeguimientoResponse;
import com.ciudadactiva.entity.Reporte;

import java.util.ArrayList;
import java.util.List;

public class ReporteDetalleResponse extends ReporteResponse {

    private AsignacionResponse asignacionActual;
    private List<SeguimientoResponse> lineaDeTiempo = new ArrayList<>();
    private List<ComentarioResponse> comentarios = new ArrayList<>();

    public ReporteDetalleResponse() {
        super();
    }

    public ReporteDetalleResponse(Reporte reporte) {
        super(reporte);
    }

    public AsignacionResponse getAsignacionActual() {
        return asignacionActual;
    }

    public void setAsignacionActual(AsignacionResponse asignacionActual) {
        this.asignacionActual = asignacionActual;
    }

    public List<SeguimientoResponse> getLineaDeTiempo() {
        return lineaDeTiempo;
    }

    public void setLineaDeTiempo(List<SeguimientoResponse> lineaDeTiempo) {
        this.lineaDeTiempo = lineaDeTiempo;
    }

    public List<ComentarioResponse> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<ComentarioResponse> comentarios) {
        this.comentarios = comentarios;
    }
}
