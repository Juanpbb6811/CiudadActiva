package com.ciudadactiva.config;

import com.ciudadactiva.entity.*;
import com.ciudadactiva.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final DataSource dataSource;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReporteRepository reporteRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            DataSource dataSource,
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ReporteRepository reporteRepository,
            SeguimientoRepository seguimientoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.dataSource = dataSource;
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.reporteRepository = reporteRepository;
        this.seguimientoRepository = seguimientoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Iniciando verificación y carga de datos iniciales en CiudadActiva...");

        // 0. Ejecución segura e idempotente de migraciones DDL para nuevas columnas y tablas optimizadas
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE reportes ADD COLUMN IF NOT EXISTS codigo_seguimiento VARCHAR(30)");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_reportes_codigo_seguimiento ON reportes(codigo_seguimiento)");
            stmt.execute("ALTER TABLE reportes ADD COLUMN IF NOT EXISTS ciudad VARCHAR(100)");
            stmt.execute("ALTER TABLE reportes ADD COLUMN IF NOT EXISTS departamento VARCHAR(100)");
            stmt.execute("ALTER TABLE reportes ADD COLUMN IF NOT EXISTS barrio VARCHAR(100)");
            stmt.execute("UPDATE reportes SET ciudad = 'Bogotá D.C.', departamento = 'Cundinamarca', barrio = 'Chapinero' WHERE ciudad IS NULL");

            // Nuevas columnas legales colombianas y de identidad para ciudadanos
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS primer_nombre VARCHAR(60)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS segundo_nombre VARCHAR(60)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS primer_apellido VARCHAR(60)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS segundo_apellido VARCHAR(60)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS tipo_documento VARCHAR(20) DEFAULT 'CC'");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS numero_documento VARCHAR(30)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS google_id VARCHAR(120)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS verificado BOOLEAN DEFAULT TRUE");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_verificacion VARCHAR(10)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_verificacion_expira TIMESTAMP WITHOUT TIME ZONE");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_recuperacion VARCHAR(10)");
            stmt.execute("ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS codigo_recuperacion_expira TIMESTAMP WITHOUT TIME ZONE");

            // Tabla dedicada para almacenamiento optimizado de imágenes en binario nativo BYTEA
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reporte_imagenes (
                    id BIGSERIAL PRIMARY KEY,
                    reporte_id BIGINT NOT NULL,
                    datos_binarios BYTEA NOT NULL,
                    tipo_mime VARCHAR(50) NOT NULL,
                    nombre_archivo VARCHAR(255),
                    peso_bytes INT NOT NULL,
                    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_reporte_img FOREIGN KEY (reporte_id) REFERENCES reportes(id) ON DELETE CASCADE
                )
            """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reporte_imagenes_reporte_id ON reporte_imagenes(reporte_id)");

            // Tabla dedicada para tokens de recuperación de contraseña hasheados con SHA-256 (estándar OWASP)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS password_reset_tokens (
                    id BIGSERIAL PRIMARY KEY,
                    user_id BIGINT NOT NULL,
                    token_hash VARCHAR(64) NOT NULL UNIQUE,
                    expiry_date TIMESTAMP NOT NULL,
                    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE
                )
            """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_reset_token_hash ON password_reset_tokens(token_hash)");

            // Asignar códigos a reportes históricos que no lo tengan
            stmt.execute("UPDATE reportes SET codigo_seguimiento = 'RAD-' || EXTRACT(YEAR FROM creado_en)::TEXT || '-' || LPAD(id::TEXT, 5, '0') WHERE codigo_seguimiento IS NULL");

            // Asegurar que todos los usuarios queden activos y verificados sin trabas
            stmt.execute("UPDATE usuarios SET verificado = TRUE, activo = TRUE WHERE verificado = FALSE OR activo = FALSE");
            log.info("Migraciones DDL aplicadas con éxito en Base de Datos.");
        } catch (Exception e) {
            log.warn("Nota sobre migración DDL: {}", e.getMessage());
        }

        // 1. Inicializar Roles
        Rol rolAdmin = inicializarRol(RolNombre.ROLE_ADMIN, "Administrador del Sistema - Control total y configuración");
        Rol rolGestor = inicializarRol(RolNombre.ROLE_GESTOR, "Gestor de Incidencias - Validación, priorización y asignación");
        Rol rolOperador = inicializarRol(RolNombre.ROLE_OPERADOR, "Operador de Campo - Ejecución, avances y cierre de labores");
        Rol rolCiudadano = inicializarRol(RolNombre.ROLE_CIUDADANO, "Ciudadano - Reporte y seguimiento de incidencias comunitarias");

        // 2. Inicializar Categorías
        Categoria catVias = inicializarCategoria("Vías y Pavimento", "Huecos, baches, hundimientos y grietas en la calzada", "road");
        Categoria catAlumbrado = inicializarCategoria("Alumbrado Público", "Postes sin luz, luminarias intermitentes o cables expuestos", "lightbulb");
        Categoria catBasuras = inicializarCategoria("Basuras y Escombros", "Acumulación de residuos, basuras en esquinas o escombros abandonados", "trash");
        Categoria catParques = inicializarCategoria("Parques y Espacios Públicos", "Mobiliario urbano roto, juegos infantiles deteriorados o zonas verdes", "park");
        Categoria catAcueducto = inicializarCategoria("Acueducto y Alcantarillado", "Tapas de alcantarilla faltantes, fugas de agua potable o taponamientos", "water");
        Categoria catSenales = inicializarCategoria("Señalización Vial", "Semáforos dañados, señales de tránsito caídas o demarcación borrada", "traffic");

        // 3. Inicializar Usuarios de Prueba
        Usuario admin = inicializarUsuario("Administrador General", "admin@ciudadactiva.gov.co", "Admin123!", "3001112233", "Palacio Municipal", rolAdmin);
        Usuario gestor = inicializarUsuario("Carlos Gestor", "gestor@ciudadactiva.gov.co", "Gestor123!", "3012223344", "Oficina de Obras Públicas", rolGestor);
        Usuario operador = inicializarUsuario("Juan Operador", "operador@ciudadactiva.gov.co", "Operador123!", "3023334455", "Base Operativa Norte", rolOperador);
        Usuario ciudadano = inicializarUsuario("Ana Ciudadana", "ciudadano@ciudadactiva.gov.co", "Ciudadano123!", "3034445566", "Cra 7 # 45-20", rolCiudadano);

        // 4. Semilla inicial de reporte demostrativo si no existen reportes
        if (reporteRepository.count() == 0) {
            Reporte reporteDemostracion = new Reporte(
                    "Hueco profundo en vía principal",
                    "Se presenta un bache de gran tamaño que pone en riesgo a ciclistas y vehículos.",
                    4.60971,
                    -74.08175,
                    "Calle 53 con Carrera 13",
                    "Bogotá D.C.",
                    "Cundinamarca",
                    "Chapinero",
                    "https://images.unsplash.com/photo-1515162816999-a0c47dc192f7",
                    catVias,
                    ciudadano
            );
            reporteDemostracion.setEstado(EstadoReporte.PENDIENTE);
            reporteDemostracion.setPrioridad(PrioridadReporte.MEDIA);
            reporteDemostracion = reporteRepository.save(reporteDemostracion);

            Seguimiento segCreacion = new Seguimiento(
                    reporteDemostracion,
                    ciudadano,
                    null,
                    EstadoReporte.PENDIENTE,
                    "CREACION_REPORTE",
                    "Incidencia reportada por el ciudadano a través de la aplicación móvil.",
                    reporteDemostracion.getImagenUrl()
            );
            seguimientoRepository.save(segCreacion);

            log.info("Reporte demostrativo inicial creado con éxito (ID: {})", reporteDemostracion.getId());
        }

        log.info("Datos iniciales de CiudadActiva cargados correctamente.");
    }

    private Rol inicializarRol(RolNombre nombre, String descripcion) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> {
            Rol r = new Rol(nombre, descripcion);
            return rolRepository.save(r);
        });
    }

    private Categoria inicializarCategoria(String nombre, String descripcion, String icono) {
        return categoriaRepository.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            Categoria c = new Categoria(nombre, descripcion, icono);
            return categoriaRepository.save(c);
        });
    }

    private Usuario inicializarUsuario(String nombre, String email, String rawPassword, String telefono, String direccion, Rol rol) {
        return usuarioRepository.findByEmail(email).map(u -> {
            u.setVerificado(true);
            u.setActivo(true);
            if (u.getNumeroDocumento() == null || u.getNumeroDocumento().isBlank()) {
                asignarDatosLegalesDemo(u, nombre);
            }
            return usuarioRepository.save(u);
        }).orElseGet(() -> {
            Usuario u = new Usuario(nombre, email, passwordEncoder.encode(rawPassword), telefono, direccion, rol);
            u.setVerificado(true);
            u.setActivo(true);
            asignarDatosLegalesDemo(u, nombre);
            return usuarioRepository.save(u);
        });
    }

    private void asignarDatosLegalesDemo(Usuario u, String nombre) {
        u.setTipoDocumento(com.ciudadactiva.entity.TipoDocumento.CC);
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 3) {
            u.setPrimerNombre(partes[0]);
            u.setSegundoNombre(partes[1]);
            u.setPrimerApellido(partes[2]);
            u.setSegundoApellido(partes.length > 3 ? partes[3] : "Gómez");
        } else if (partes.length == 2) {
            u.setPrimerNombre(partes[0]);
            u.setPrimerApellido(partes[1]);
            u.setSegundoApellido("Gómez");
        } else {
            u.setPrimerNombre(nombre);
            u.setPrimerApellido("Oficial");
            u.setSegundoApellido("Ciudadano");
        }
        if (u.getEmail().contains("admin")) u.setNumeroDocumento("1010101010");
        else if (u.getEmail().contains("gestor")) u.setNumeroDocumento("1020202020");
        else if (u.getEmail().contains("operador")) u.setNumeroDocumento("1030303030");
        else u.setNumeroDocumento("1040404040");
    }
}
