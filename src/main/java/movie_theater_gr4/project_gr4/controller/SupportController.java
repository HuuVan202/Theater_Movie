package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.dto.SupportRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/support")
public class SupportController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitSupportRequest(@Valid @RequestBody SupportRequestDTO request, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        // Check for validation errors using BindingResult
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("success", false);
            response.put("errors", errors);
            return ResponseEntity.badRequest().body(response);
        }

        // Additional validation for supportTopic
        if ("Chọn chủ đề".equals(request.getSupportTopic())) {
            response.put("success", false);
            response.put("errors", Map.of("supportTopic", "Vui lòng chọn một chủ đề hợp lệ"));
            return ResponseEntity.badRequest().body(response);
        }

        try {
            // Load email template
            ClassPathResource resource = new ClassPathResource("templates/formatEmail.html");
            String emailContent = Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);

            // Populate email template
            emailContent = emailContent.replace("{fullName}", request.getFullName())
                    .replace("{phoneNumber}", request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() ? request.getPhoneNumber() : "Không cung cấp")
                    .replace("{email}", request.getEmail())
                    .replace("{supportTopic}", request.getSupportTopic())
                    .replace("{subject}", request.getSubject())
                    .replace("{description}", request.getDescription().replace("\n", "<br>"));

            // Send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo("mooncinema01@gmail.com");
            helper.setFrom(request.getEmail());
            helper.setSubject(request.getSubject());
            helper.setText(emailContent, true);
            mailSender.send(message);

            response.put("success", true);
            response.put("message", "Yêu cầu hỗ trợ đã được gửi thành công");
            response.put("data", request);
            return ResponseEntity.ok(response);
        } catch (MessagingException e) {
            response.put("success", false);
            response.put("errors", Map.of("general", "Lỗi khi gửi email. Vui lòng thử lại sau."));
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", Map.of("general", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
            return ResponseEntity.status(500).body(response);
        }
    }
}