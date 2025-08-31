package movie_theater_gr4.project_gr4.controller;

import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChangePassController {

    private final AccountService accountService;
    private static final Logger logger = LoggerFactory.getLogger(ChangePassController.class);

    public ChangePassController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/changePassword")
    public String changePassword(HttpSession session, Model model) {

        String email = (String) session.getAttribute("email");
        logger.info("Accessing changePassword, email in session: {}", email);

        // Để kiểm tra xem người dùng có nhập Email chưa
        if (email == null) {
            session.setAttribute("errorEmail", "Vui lòng nhập Email cần khôi phục.");
            return "redirect:/forgot";
        }

        // Để check người dùng nhập OTP chưa
        String otp = (String) session.getAttribute("otp");
        System.out.println("otp Session: " + otp);
        if (otp == null) {
            session.setAttribute("errorOTP", "Please verify OTP.");
            return "redirect:/verifyOTP";
        }

        Account account = accountService.findAccountByEmail(email);
        System.out.println(account);

        return "change_password";
    }

    @PostMapping("/changePassword")
    public String changePassword(
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {

        String email = (String) session.getAttribute("email");
        logger.info("Session ID: {}, Email: {}", session.getId(), email);
        if (email == null) {
            model.addAttribute("error", "Session expired or invalid.");
            return "redirect:/forgot";
        }

        if (password.length() < 8) {
            model.addAttribute("error", "Mật khẩu phải từ 8 ký tự!");
            return "change_password";
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$")) {
            model.addAttribute("error", "Mật khẩu không hợp lệ!");
            return "change_password";
        }

        if (!confirmPassword.equals(password)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "change_password";
        }

        Account account = accountService.findAccountByEmail(email);
        if (account == null) {
            model.addAttribute("error", "Account not found!");
            return "change_password";
        } else {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            account.setPassword(passwordEncoder.encode(password));
            accountService.updateAccount(account);
        }

        // Xóa session sau khi đổi mật khẩu thành công
        session.removeAttribute("email");
        session.removeAttribute("otpToken");
        return "redirect:/auth?forgot=true";
    }
}