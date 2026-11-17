/**
 * Entorno de DESARROLLO (ng serve). Apunta al backend Spring Boot local.
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080',
  apiPresets: [
    { label: 'Local (localhost:8080)', url: 'http://localhost:8080' },
    { label: 'Render (ciudadactiva-backend)', url: 'https://ciudadactiva-backend.onrender.com' },
    { label: 'Render (despliegue actual del repo)', url: 'https://ciudadactiva-4ah7.onrender.com' },
  ],
  appMovilPath: '/descargar',
};
