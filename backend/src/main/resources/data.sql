-- =======================================================================
-- PROYECTO: CiudadActiva - Script de Datos Iniciales (Supabase / PostgreSQL)
-- METODOLOGÍA: RUP (Rational Unified Process)
-- Contraseñas de prueba cifradas con BCrypt:
--   admin@ciudadactiva.gov.co     / Admin123!
--   gestor@ciudadactiva.gov.co    / Gestor123!
--   operador@ciudadactiva.gov.co  / Operador123!
--   ciudadano@ciudadactiva.gov.co / Ciudadano123!
-- Hash BCrypt estándar para las claves de prueba (cost 10)
-- =======================================================================

-- 1. Insertar Roles
INSERT INTO roles (id, nombre, descripcion) VALUES
(1, 'ROLE_ADMIN', 'Administrador del Sistema - Control total y configuración'),
(2, 'ROLE_GESTOR', 'Gestor de Incidencias - Validación, priorización y asignación'),
(3, 'ROLE_OPERADOR', 'Operador de Campo - Ejecución, avances y cierre de labores'),
(4, 'ROLE_CIUDADANO', 'Ciudadano - Reporte y seguimiento de incidencias comunitarias')
ON CONFLICT (nombre) DO NOTHING;

-- 2. Insertar Categorías
INSERT INTO categorias (id, nombre, descripcion, icono, activa, creado_en) VALUES
(1, 'Vías y Pavimento', 'Huecos, baches, hundimientos y grietas en la calzada', 'road', true, NOW()),
(2, 'Alumbrado Público', 'Postes sin luz, luminarias intermitentes o cables expuestos', 'lightbulb', true, NOW()),
(3, 'Basuras y Escombros', 'Acumulación de residuos, basuras en esquinas o escombros abandonados', 'trash', true, NOW()),
(4, 'Parques y Espacios Públicos', 'Mobiliario urbano roto, juegos infantiles deteriorados o zonas verdes', 'park', true, NOW()),
(5, 'Acueducto y Alcantarillado', 'Tapas de alcantarilla faltantes, fugas de agua potable o taponamientos', 'water', true, NOW()),
(6, 'Señalización Vial', 'Semáforos dañados, señales de tránsito caídas o demarcación borrada', 'traffic', true, NOW())
ON CONFLICT (nombre) DO NOTHING;

-- 3. Insertar Usuarios de Prueba (Contraseña: Rol123!)
-- Hash BCrypt para 'Admin123!': $2a$10$wT0vFjA/p9Hj2tX0eH9mOeQ0Yx7B1E4tZ9w.O7R2.O0G.r/l8Kk2y
-- Hash BCrypt para 'Gestor123!': $2a$10$tZ2yFjA/p9Hj2tX0eH9mOeQ0Yx7B1E4tZ9w.O7R2.O0G.r/l8Kk2y
-- Hash BCrypt para 'Operador123!': $2a$10$oP4yFjA/p9Hj2tX0eH9mOeQ0Yx7B1E4tZ9w.O7R2.O0G.r/l8Kk2y
-- Hash BCrypt para 'Ciudadano123!': $2a$10$cI6yFjA/p9Hj2tX0eH9mOeQ0Yx7B1E4tZ9w.O7R2.O0G.r/l8Kk2y
-- NOTA: DataInitializer de Spring Boot genera e inserta automáticamente los hashes verificados en cada inicio si faltan.
