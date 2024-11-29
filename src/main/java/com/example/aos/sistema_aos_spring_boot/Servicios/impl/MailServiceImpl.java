package com.example.aos.sistema_aos_spring_boot.Servicios.impl;


import com.example.aos.sistema_aos_spring_boot.Modelo.EmailLog;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.EmailLimitExceededException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.EmailSendingException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.SpecificEmailException;
import com.example.aos.sistema_aos_spring_boot.Modelo.Exceptions.UserNotFoundException;
import com.example.aos.sistema_aos_spring_boot.Modelo.ResetPasswordEntity;
import com.example.aos.sistema_aos_spring_boot.Modelo.Usuario;
import com.example.aos.sistema_aos_spring_boot.Modelo.VerificationCodeEntity;
import com.example.aos.sistema_aos_spring_boot.Repositorios.EmailLogRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.ResetPasswordRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.UsuarioRepository;
import com.example.aos.sistema_aos_spring_boot.Repositorios.VerificationCodeRepository;
import com.example.aos.sistema_aos_spring_boot.Servicios.MailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MailServiceImpl implements MailService {
    private final static String MY_EMAIL = "evaclub@webpageica.com";
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${api.key}")
    private String apiKey;

    @Value("${app.verification.url}")
    private String verificationUrl;

    @Value("${app.reset.url}")
    private String resetUrl;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailLogRepository emailLogRepository;

    private final OkHttpClient client = new OkHttpClient();

    private void sendEmail(String to, String subject, String htmlContent) throws IOException {

        String escapedHtmlContent = htmlContent
                .replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "");

        String json = "{"
                + "\"sender\": {\"name\": \"EvaClub\", \"email\": \"" + MY_EMAIL + "\"},"
                + "\"to\": [{\"email\": \"" + to + "\", \"name\": \"Nombre Destinatario\"}],"
                + "\"subject\": \"" + subject + "\","
                + "\"htmlContent\": \"" + escapedHtmlContent + "\""
                + "}";

        System.out.println("JSON Enviado: " + json);

        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorResponse = response.body() != null ? response.body().string() : "No response body";
                throw new IOException("Error al enviar el correo: " + response.message() + " - " + errorResponse);
            }
        }
    }



    @Override
    public void sendVerificationEmail(String email) throws SpecificEmailException {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado.");
        }

        long todayCount = emailLogRepository.countByUsuarioAndEmailTypeAndSentAtAfter(
                usuario, "verification", LocalDateTime.now().minusDays(1));

        if (todayCount >= 2) {
            throw new RuntimeException("Límite de correos alcanzado para hoy. Si necesitas ayuda contacta al soporte técnico +51 970 932 182.");
        }

        String verificationCode = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15);

        VerificationCodeEntity verificationEntity = new VerificationCodeEntity();
        verificationEntity.setUsuario(usuario);
        verificationEntity.setCode(verificationCode);
        verificationEntity.setExpirationTime(expirationTime);
        verificationCodeRepository.save(verificationEntity);

        String verificationLink = verificationUrl + "?code=" + verificationCode;

        Context context = new Context();
        context.setVariable("name", usuario.getNombre());
        context.setVariable("verificationLink", verificationLink);

        String htmlContent = templateEngine.process("verificationTemplate.html", context);

        try {
            sendEmail(email, "Verificación de Cuenta", htmlContent);

            EmailLog emailLog = new EmailLog();
            emailLog.setUsuario(usuario);
            emailLog.setEmailType("verification");
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);
        } catch (IOException e) {
            throw new SpecificEmailException("Error al enviar el correo de verificación."+ e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(String email) throws SpecificEmailException {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            throw new UserNotFoundException("Usuario no encontrado.");
        }

        long todayCount = emailLogRepository.countByUsuarioAndEmailTypeAndSentAtAfter(
                usuario, "reset", LocalDateTime.now().minusDays(1));

        if (todayCount >= 2) {
            throw new EmailLimitExceededException("Límite de correos alcanzado para hoy. Si necesitas ayuda contacta al soporte técnico +51 970 932 182.");
        }

        String code = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15);

        ResetPasswordEntity resetPasswordEntity = new ResetPasswordEntity();
        resetPasswordEntity.setUsuario(usuario);
        resetPasswordEntity.setCode(code);
        resetPasswordEntity.setExpirationTime(expirationTime);
        resetPasswordRepository.save(resetPasswordEntity);

        String resetLink = resetUrl + "?code=" + code;

        Context context = new Context();
        context.setVariable("name", usuario.getNombre());
        context.setVariable("resetLink", resetLink);

        String htmlContent = templateEngine.process("resetPasswordTemplate.html", context);

        try {
            sendEmail(email, "Restablecimiento de Contraseña", htmlContent);

            EmailLog emailLog = new EmailLog();
            emailLog.setUsuario(usuario);
            emailLog.setEmailType("reset");
            emailLog.setSentAt(LocalDateTime.now());
            emailLogRepository.save(emailLog);
        } catch (IOException e) {
            throw new SpecificEmailException("Error al enviar el correo de cambio de contraseña."+ e.getMessage());
        }
    }
}


