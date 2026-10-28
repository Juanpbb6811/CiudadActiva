package com.ciudadactiva.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${app.mail.provider:resend}")
    private String mailProvider;

    @Value("${app.mail.from:CiudadActiva <onboarding@resend.dev>}")
    private String mailFrom;

    @Value("${app.mail.resend-api-key:}")
    private String resendApiKey;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * Envía correo con código OTP de activación de cuenta.
     */
    public boolean enviarCodigoVerificacion(String toEmail, String nombreUsuario, String codigoOtp) {
        String asunto = "Verifica tu cuenta - CiudadActiva (" + codigoOtp + ")";
        String html = construirHtmlOtp(
                "¡Bienvenido a CiudadActiva!",
                "completar tu registro y activar tu cuenta ciudadana",
                nombreUsuario != null && !nombreUsuario.isBlank() ? nombreUsuario : "Ciudadano",
                codigoOtp,
                15
        );
        return despacharCorreo(toEmail, asunto, html, codigoOtp, "VERIFICACIÓN");
    }

    /**
     * Envía correo con código OTP de recuperación de contraseña.
     */
    public boolean enviarCodigoRecuperacion(String toEmail, String nombreUsuario, String codigoOtp) {
        String asunto = "Recuperación de Contraseña - CiudadActiva (" + codigoOtp + ")";
        String html = construirHtmlOtp(
                "Recuperación de Acceso",
                "restablecer la contraseña de acceso a tu cuenta",
                nombreUsuario != null && !nombreUsuario.isBlank() ? nombreUsuario : "Ciudadano",
                codigoOtp,
                15
        );
        return despacharCorreo(toEmail, asunto, html, codigoOtp, "RECUPERACIÓN");
    }

    private boolean despacharCorreo(String toEmail, String asunto, String html, String codigoOtp, String tipo) {
        log.info("🔐 [OTP Service] Solicitud de {}: Destino=[{}], Código OTP=[{}]", tipo, toEmail, codigoOtp);
        boolean enviado = false;

        // 1. Intento con Resend API (HTTP REST en puerto 443, no bloqueado por firewalls de hosting)
        if (resendApiKey != null && !resendApiKey.isBlank() && !"smtp".equalsIgnoreCase(mailProvider)) {
            try {
                enviado = enviarViaResend(toEmail, asunto, html);
                if (enviado) {
                    log.info("📧 Correo de {} enviado con éxito a {} mediante Resend API.", tipo, toEmail);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Fallo al enviar correo con Resend API ({}): {}. Intentando alternativa...", tipo, e.getMessage());
            }
        }

        // 2. Intento con Brevo API (HTTP REST en puerto 443, no bloqueado por firewalls de hosting)
        if (brevoApiKey != null && !brevoApiKey.isBlank() && !"smtp".equalsIgnoreCase(mailProvider)) {
            try {
                enviado = enviarViaBrevo(toEmail, asunto, html);
                if (enviado) {
                    log.info("📧 Correo de {} enviado con éxito a {} mediante Brevo API.", tipo, toEmail);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Fallo al enviar correo con Brevo API ({}): {}. Intentando alternativa...", tipo, e.getMessage());
            }
        }

        // 3. Intento con SMTP (JavaMailSender / Gmail / Brevo SMTP)
        if (javaMailSender != null && smtpUsername != null && !smtpUsername.isBlank()) {
            try {
                enviado = enviarViaSmtp(toEmail, asunto, html);
                if (enviado) {
                    log.info("📧 Correo de {} enviado con éxito a {} mediante SMTP ({}).", tipo, toEmail, smtpUsername);
                    return true;
                }
            } catch (Exception e) {
                log.warn("Fallo al enviar correo con SMTP ({}): {}. (Nota: Render capa gratuita bloquea puertos SMTP 25, 465 y 587. Configura RESEND_API_KEY o BREVO_API_KEY para envío HTTPS garantizado en Render).", tipo, e.getMessage());
            }
        }

        // 3. Si no hay credenciales configuradas en el entorno, loguear para depuración en servidor
        log.warn("================================================================================");
        log.warn("⚠️ AVISO DEL SERVICIO DE CORREO - CIUDADACTIVA");
        log.warn("No se detectó RESEND_API_KEY ni credenciales SMTP configuradas en las variables de entorno.");
        log.warn("Solicitud de {}: Correo destino: {}", tipo, toEmail);
        log.warn("Código OTP generado: {}", codigoOtp);
        log.warn("Para que los correos lleguen a la bandeja real del usuario, configura:");
        log.warn("  - En Render: RESEND_API_KEY=re_tu_clave (Gratis 3.000 emails/mes en https://resend.com)");
        log.warn("  - O bien SMTP: SPRING_MAIL_USERNAME y SPRING_MAIL_PASSWORD (App Password de Gmail)");
        log.warn("================================================================================");

        return false;
    }

    private boolean enviarViaResend(String toEmail, String asunto, String html) throws Exception {
        // Escapar caracteres en el JSON
        String jsonPayload = String.format(
                "{\"from\":%s,\"to\":[%s],\"subject\":%s,\"html\":%s}",
                escapeJson(mailFrom),
                escapeJson(toEmail),
                escapeJson(asunto),
                escapeJson(html)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey.trim())
                .header("Content-Type", "application/json")
                .header("User-Agent", "CiudadActiva-Backend/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true;
        } else {
            throw new RuntimeException("Resend API retornó status " + response.statusCode() + ": " + response.body());
        }
    }

    private boolean enviarViaBrevo(String toEmail, String asunto, String html) throws Exception {
        String remitenteEmail = (smtpUsername != null && !smtpUsername.isBlank()) ? smtpUsername : "juanpablobejarano0@gmail.com";
        String jsonPayload = String.format(
                "{\"sender\":{\"name\":\"CiudadActiva\",\"email\":%s},\"to\":[{\"email\":%s}],\"subject\":%s,\"htmlContent\":%s}",
                escapeJson(remitenteEmail),
                escapeJson(toEmail),
                escapeJson(asunto),
                escapeJson(html)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", brevoApiKey.trim())
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("User-Agent", "CiudadActiva-Backend/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return true;
        } else {
            throw new RuntimeException("Brevo API retornó status " + response.statusCode() + ": " + response.body());
        }
    }

    private boolean enviarViaSmtp(String toEmail, String asunto, String html) {
        if (javaMailSender == null) return false;
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(smtpUsername, "CiudadActiva");
            helper.setTo(toEmail);
            helper.setSubject(asunto);
            helper.setText(html, true);
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Error enviando por JavaMailSender: {}", e.getMessage());
            return false;
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String construirHtmlOtp(String encabezadoAccion, String descripcionAccion, String nombreUsuario, String codigoOtp, int minutosExpira) {
        return "<!DOCTYPE html>" +
                "<html lang=\"es\">" +
                "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>" +
                "<body style=\"margin: 0; padding: 0; background-color: #f4f6f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1e293b;\">" +
                "  <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #f4f6f9; padding: 30px 15px;\">" +
                "    <tr><td align=\"center\">" +
                "      <table role=\"presentation\" width=\"100%\" style=\"max-width: 520px; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); border: 1px solid #e2e8f0;\">" +
                "        <tr>" +
                "          <td style=\"background: linear-gradient(135deg, #1b4941 0%, #2b7a69 100%); padding: 30px; text-align: center;\">" +
                "            <h1 style=\"margin: 0; color: #ffffff; font-size: 24px; font-weight: 800; letter-spacing: -0.5px;\">🏛️ CiudadActiva</h1>" +
                "            <p style=\"margin: 6px 0 0 0; color: #d0f0e8; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: 1.2px;\">Plataforma Oficial de Gestión Comunitaria</p>" +
                "          </td>" +
                "        </tr>" +
                "        <tr>" +
                "          <td style=\"padding: 32px 28px;\">" +
                "            <h2 style=\"margin: 0 0 12px 0; color: #1b4941; font-size: 20px; font-weight: 700;\">" + encabezadoAccion + "</h2>" +
                "            <p style=\"margin: 0 0 20px 0; color: #475569; font-size: 14px; line-height: 1.6;\">" +
                "              Hola <strong>" + nombreUsuario + "</strong>, hemos recibido una solicitud para <strong>" + descripcionAccion + "</strong> en CiudadActiva." +
                "            </p>" +
                "            <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin: 24px 0;\">" +
                "              <tr>" +
                "                <td align=\"center\" style=\"background-color: #f0fdf9; border: 2px dashed #99f6e4; border-radius: 14px; padding: 22px;\">" +
                "                  <span style=\"display: block; font-size: 11px; font-weight: 700; color: #0d9488; text-transform: uppercase; letter-spacing: 1.5px; margin-bottom: 6px;\">Tu Código de Verificación OTP</span>" +
                "                  <span style=\"display: block; font-size: 38px; font-weight: 900; color: #115e59; letter-spacing: 8px; font-family: 'Courier New', monospace;\">" + codigoOtp + "</span>" +
                "                  <span style=\"display: inline-block; margin-top: 10px; background-color: #fef2f2; color: #991b1b; font-size: 11px; font-weight: 700; padding: 4px 12px; border-radius: 20px;\">⏱️ Vence en " + minutosExpira + " minutos</span>" +
                "                </td>" +
                "              </tr>" +
                "            </table>" +
                "            <table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: #f8fafc; border-radius: 10px; padding: 12px 16px; border: 1px solid #e2e8f0;\">" +
                "              <tr><td style=\"color: #64748b; font-size: 12px; line-height: 1.5;\">🛡️ <strong>Seguridad:</strong> Nunca compartas este código con nadie. El personal de la Alcaldía o CiudadActiva jamás te solicitará este código.</td></tr>" +
                "            </table>" +
                "            <p style=\"margin: 20px 0 0 0; color: #94a3b8; font-size: 12px; line-height: 1.5;\">Si no solicitaste este código, puedes ignorar este mensaje de forma segura.</p>" +
                "          </td>" +
                "        </tr>" +
                "        <tr>" +
                "          <td style=\"background-color: #f8fafc; border-top: 1px solid #e2e8f0; padding: 20px; text-align: center;\">" +
                "            <p style=\"margin: 0; color: #64748b; font-size: 11px;\">© 2026 CiudadActiva - Colombia. Sistema de Trazabilidad Ciudadana.</p>" +
                "          </td>" +
                "        </tr>" +
                "      </table>" +
                "    </td></tr>" +
                "  </table>" +
                "</body>" +
                "</html>";
    }
}
