/**
 * Entorno de PRODUCCIÓN (ng build).
 * La URL activa puede cambiarse en tiempo de ejecución desde la pantalla de login (selector "Servidor"),
 * y queda guardada en localStorage bajo la clave `ca.apiUrl`.
 */
export const environment = {
  production: true,
  apiUrl: 'https://ciudadactiva-4ah7.onrender.com',
  apiPresets: [
    { label: 'Local (localhost:8080)', url: 'http://localhost:8080' },
    { label: 'Render (ciudadactiva-backend)', url: 'https://ciudadactiva-backend.onrender.com' },
    { label: 'Render (despliegue actual del repo)', url: 'https://ciudadactiva-4ah7.onrender.com' },
  ],
  /** URL pública de descarga de la app móvil (para ciudadanos y operadores). */
  appMovilPath: '/descargar',
};
