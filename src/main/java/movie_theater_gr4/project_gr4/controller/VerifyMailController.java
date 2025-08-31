package movie_theater_gr4.project_gr4.controller;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.model.OTPData;
import movie_theater_gr4.project_gr4.service.AccountService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class VerifyMailController {

    private final AccountService accountService;
    private final JavaMailSender mailSender;
    private final ScheduledExecutorService scheduler;
    private static final String MAIL_ADMIN = "mooncinema01@gmail.com";

    public VerifyMailController(AccountService accountService, JavaMailSender mailSender, ScheduledExecutorService scheduler) {
        this.accountService = accountService;
        this.mailSender = mailSender;
        this.scheduler = scheduler;
    }

    @GetMapping("/forgot")
    public String verifyMail(HttpSession session, Model model) {
//        String email = (String) session.getAttribute("email");
//        if (email != null) {
//            model.addAttribute("email", email);
//        }

        //Kiểm tra xem người dùng có nhập mail chưa mà truy cập
        if (session.getAttribute("errorEmail") != null) {
            model.addAttribute("error", session.getAttribute("errorEmail"));
        }

        // xóa câu báo lỗi
        session.removeAttribute("errorEmail");
        return "verify_mail";
    }

    @PostMapping("/forgot")
    public String forgotMail(
            @RequestParam("email") String email,
            Model model,
            HttpSession session) {

        //Check thử xem tài khoản có email này có tồn tại khoong
        if (!accountService.isExistingEmail(email)) {
            // Gửi error nếu tài khoản không toon tại
            model.addAttribute("error", "Email không tồn tại.");
            return "verify_mail";
        }

        if (accountService.isAccountIsGoogle(email)) {
            model.addAttribute("error", "Tài khoản được đăng kí bằng tài khoản Google. " +
                    "Vui lòng đăng bằng Google.");
            return "verify_mail";
        }

        //Kiểm tra nếu có token cũ thì xóa để tránh lạm dụng OTP
        String oldToken = (String) session.getAttribute("otpToken");
        if (oldToken != null) {
            OTPData.getOtpStore().remove(oldToken);
        }

        // Tạo ra OTP mới
        String otp = generateOTP();
        System.out.println("otp: " + otp);
        // Tạo token mới
        String token = UUID.randomUUID().toString();

        try {
            // Gọi method gửi mail
            sendOTPEmail(email, otp);

            OTPData.getOtpStore().put(token, new OTPData(email, otp, System.currentTimeMillis()));
            session.setAttribute("otpToken", token);
            session.setAttribute("email", email);
            // sống lần cho phép người dùng nhập sai
            session.setAttribute("otpAttempts", 4);
            // Gọi hàm để hết thời gian thì xóa OTP
            scheduleOTPCleanup(token);
            // Sau khi người dùng gửi lại OTP thì thông báo
            model.addAttribute("info", "Mã OTP mới đã được gửi đến email của bạn!");
//            model.addAttribute("email", email);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể gửi mã OTP. Vui lòng thử lại.");
            model.addAttribute("email", email);
            return "verify_mail";
        }

        return "redirect:/verifyOTP";
    }

    private String generateOTP() {
        return String.format("%06d", new java.util.Random().nextInt(1000000));
    }

    private void sendOTPEmail(String email, String otp) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

        helper.setFrom(MAIL_ADMIN);
        helper.setTo(email);
        helper.setSubject("Verify Mail");

        try (var inputStream = Objects.requireNonNull(getClass().getResourceAsStream("/templates/email/emailOTP.html"))) {
            String emailTemplate = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            emailTemplate = emailTemplate.replace("${otpCode}", otp);
            helper.setText(emailTemplate, true);
        }

        mailSender.send(mimeMessage);
    }

    private void scheduleOTPCleanup(String token) {
        scheduler.schedule(() -> OTPData.getOtpStore().remove(token), 5, TimeUnit.MINUTES);
    }
}