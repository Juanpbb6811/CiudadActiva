package com.ciudadactiva.app.data.local

data class DemoAccount(
    val rolTag: String,
    val label: String,
    val email: String,
    val pass: String,
    val rolEnum: String
)

object DemoCredentials {
    val accounts = listOf(
        DemoAccount(
            rolTag = "CIUDADANO",
            label = "Ciudadano",
            email = "ciudadano@ciudadactiva.gov.co",
            pass = "Ciudadano123!",
            rolEnum = "ROLE_CIUDADANO"
        ),
        DemoAccount(
            rolTag = "GESTOR",
            label = "Gestor",
            email = "gestor@ciudadactiva.gov.co",
            pass = "Gestor123!",
            rolEnum = "ROLE_GESTOR"
        ),
        DemoAccount(
            rolTag = "OPERADOR",
            label = "Operador",
            email = "operador@ciudadactiva.gov.co",
            pass = "Operador123!",
            rolEnum = "ROLE_OPERADOR"
        ),
        DemoAccount(
            rolTag = "ADMIN",
            label = "Admin",
            email = "admin@ciudadactiva.gov.co",
            pass = "Admin123!",
            rolEnum = "ROLE_ADMIN"
        )
    )
}
