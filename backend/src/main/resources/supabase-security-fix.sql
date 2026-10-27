-- =======================================================================
-- PROYECTO: CiudadActiva - Script de Seguridad y RLS para Supabase
-- OBJETIVO: Resolver las 8 advertencias críticas del Supabase Security Advisor:
--           - RLS Disabled in Public (7 tablas)
--           - Sensitive Columns Exposed (public.usuarios.password)
-- =======================================================================

-- 1. Habilitar Row Level Security (RLS) en todas las tablas del esquema público.
--    Esto bloquea el acceso anónimo o no autorizado a través de la API PostgREST de Supabase.
ALTER TABLE public.roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.categorias ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reportes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.asignaciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.seguimientos ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comentarios ENABLE ROW LEVEL SECURITY;

-- 2. Revocar todos los permisos directos a los roles públicos de Supabase ('anon' y 'authenticated').
--    Dado que CiudadActiva utiliza un backend centralizado en Spring Boot (Render) que se conecta
--    directamente por JDBC como usuario propietario ('postgres'), las llamadas externas a la API REST
--    automática de Supabase quedan completamente clausuradas, protegiendo las contraseñas y datos.
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL ROUTINES IN SCHEMA public FROM anon, authenticated;

-- NOTA DE COMPATIBILIDAD:
-- El usuario 'postgres' (utilizado por Spring Boot en Render) es SUPERUSER / OWNER
-- de la base de datos, por lo que PostgreSQL le permite eludir (BYPASS) el RLS de forma nativa.
-- Toda la funcionalidad de la aplicación móvil y el backend continuará funcionando al 100%.
