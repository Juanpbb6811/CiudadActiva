package com.ciudadactiva.config;

import com.ciudadactiva.entity.*;
import com.ciudadactiva.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReporteRepository reporteRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ReporteRepository reporteRepository,
            SeguimientoRepository seguimientoRepository,
            PasswordEncoder passwordEncoder
    ) {
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
        return usuarioRepository.findByEmail(email).orElseGet(() -> {
            Usuario u = new Usuario(nombre, email, passwordEncoder.encode(rawPassword), telefono, direccion, rol);
            return usuarioRepository.save(u);
        });
    }
}
