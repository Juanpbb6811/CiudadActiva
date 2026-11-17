import { JwtClaims } from '../api/models';

/** Decodifica el payload de un JWT (sin verificar firma: eso lo hace el backend en cada petición). */
export function decodificarJwt(token: string): JwtClaims | null {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    );
    return JSON.parse(json) as JwtClaims;
  } catch {
    return null;
  }
}

export function jwtExpirado(claims: JwtClaims | null, margenSegundos = 10): boolean {
  if (!claims?.exp) return true;
  return Date.now() / 1000 >= claims.exp - margenSegundos;
}
