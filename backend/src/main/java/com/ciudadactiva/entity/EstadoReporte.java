package com.ciudadactiva.entity;

public enum EstadoReporte {
    PENDIENTE,      // Reporte registrado por el ciudadano
    EN_REVISION,    // Gestor examinando la veracidad del reporte
    ADMITIDO,       // Gestor validó positivamente el reporte
    RECHAZADO,      // Gestor rechazó el reporte (debe incluir justificación)
    EN_PROCESO,     // Asignado a un operador que realiza trabajos
    ATENDIDO,       // Operador finalizó los trabajos de atención en campo
    CERRADO         // Incidente cerrado con evidencia de solución confirmada
}
