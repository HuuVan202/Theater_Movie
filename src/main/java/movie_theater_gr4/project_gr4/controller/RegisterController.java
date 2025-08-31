package movie_theater_gr4.project_gr4.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import movie_theater_gr4.project_gr4.dto.AccountGGDTO;
import movie_theater_gr4.project_gr4.dto.AccountRegisterDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.security.CustomUserDetails;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
public class RegisterController {

    private final AccountService accountService;
    private final NotificationService notificationService;

    @Autowired
    public RegisterController(AccountService accountService,  NotificationService notificationService) {
        this.accountService = accountService;
        this.notificationService = notificationService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDTO", new AccountRegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(@Valid @ModelAttribute("registerDTO") AccountRegisterDTO accountRegisterDTO,
                               BindingResult bindingResult) {
        accountRegisterDTO.trimFields();

        // Check for validation errors from annotations
        if (!bindingResult.hasErrors()) {
            // Custom validations
            if (!accountRegisterDTO.getPassword().equals(accountRegisterDTO.getPasswordConfirm())) {
                bindingResult.addError(new FieldError("registerDTO", "passwordConfirm", "Passwords do not match"));
            }
            if (accountService.isExistingAccount(accountRegisterDTO.getUsername())) {
                bindingResult.addError(new FieldError("registerDTO", "username", "Username is already in use"));
            }
            if (accountService.isExistingEmail(accountRegisterDTO.getEmail())) {
                bindingResult.addError(new FieldError("registerDTO", "email", "Email is already in use"));
            }
        }

        // If there are any errors, return to the auth page with register tab active
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // Create account and redirect on success
        int accountId = accountService.createAccount(accountRegisterDTO);

        String title = "Chào mừng bạn đến với MoonCinema";
        String content = "Cảm ơn bạn đã tham gia! Hãy khám phá thế giới phim ảnh hấp dẫn và đặt vé nhanh chóng, " +
                "tiện lợi chỉ với vài cú nhấp chuột. Đừng bỏ lỡ những suất chiếu mới nhất nhé!✨";
        notificationService.sendAndSaveNotification(accountId, title, content);

        return "redirect:/auth?success=true";
    }

    @GetMapping("/register-google")
    public String registerGG(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/auth";
        }

        try {
            String email = principal.getAttribute("email");
            String name = principal.getAttribute("name");
            String picture = principal.getAttribute("picture");

            AccountGGDTO accountGGDTO = AccountGGDTO.builder()
                    .fullName(name)
                    .email(email)
                    .avatarUrl(picture)
                    .build();

            // Tạo hoặc lấy tài khoản hiện có
            Account account = accountService.createAccountByGG(accountGGDTO);

            int accountId = account.getAccountId();
            String title = "Chào mừng bạn đến với MoonCinema";
            String content = "Cảm ơn bạn đã tham gia! Hãy khám phá thế giới phim ảnh hấp dẫn và đặt vé nhanh chóng, " +
                    "tiện lợi chỉ với vài cú nhấp chuột. Đừng bỏ lỡ những suất chiếu mới nhất nhé!✨";
            notificationService.sendAndSaveNotification(accountId, title, content);

            // Lấy chi tiết tài khoản đầy đủ
            Account fullAccount = accountService.getFullAccountDetails(email);

            // Tạo CustomUserDetails và đặt vào ngữ cảnh bảo mật
            CustomUserDetails userDetails = new CustomUserDetails(fullAccount);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Lưu chi tiết người dùng vào session nếu cần
            session.setAttribute("user", userDetails);

            return "redirect:/home";
        } catch (IllegalStateException e) {
            // Xóa ngữ cảnh xác thực để chặn đăng nhập
            SecurityContextHolder.clearContext();
            // Xóa session để đảm bảo không giữ trạng thái xác thực
            session.invalidate();
            // Xử lý trường hợp email đã được sử dụng bởi tài khoản không phải Google
            return "redirect:/auth?emailInUse=true";
        } catch (Exception e) {
            // Xóa ngữ cảnh xác thực để chặn đăng nhập
            SecurityContextHolder.clearContext();
            // Xóa session để đảm bảo không giữ trạng thái xác thực
            session.invalidate();
            System.err.println("Lỗi trong quá trình đăng nhập Google: " + e.getMessage());
            return "redirect:/auth";
        }
    }
}