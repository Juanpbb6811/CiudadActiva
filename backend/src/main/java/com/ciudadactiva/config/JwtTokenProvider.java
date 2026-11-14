package com.ciudadactiva.config;

import com.ciudadactiva.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:CiudadActivaSecretKey2026SecureKeyWithMoreThan256BitsLongForHMACSHA256Algorithm}") String secret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        String rolNombre = (usuario.getRol() != null) ? usuario.getRol().getNombre().name() : "ROLE_CIUDADANO";
        String tipoDoc = (usuario.getTipoDocumento() != null) ? usuario.getTipoDocumento().name() : "CC";
        String numDoc = (usuario.getNumeroDocumento() != null) ? usuario.getNumeroDocumento() : "";

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", rolNombre)
                .claim("tipoDocumento", tipoDoc)
                .claim("numeroDocumento", numDoc)
                .claim("telefono", usuario.getTelefono() != null ? usuario.getTelefono() : "")
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    public Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String obtenerEmailDelToken(String token) {
        return obtenerClaims(token).getSubject();
    }

    public Long obtenerUserIdDelToken(String token) {
        Object val = obtenerClaims(token).get("userId");
        if (val instanceof Number num) {
            return num.longValue();
        }
        return null;
    }

    public String obtenerRolDelToken(String token) {
        Object rol = obtenerClaims(token).get("rol");
        return rol != null ? rol.toString() : "ROLE_CIUDADANO";
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
