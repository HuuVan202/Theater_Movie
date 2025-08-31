package movie_theater_gr4.project_gr4.employee.controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import movie_theater_gr4.project_gr4.dto.AccountDTO;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Member;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.CloudinaryService;
import movie_theater_gr4.project_gr4.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/employee")
public class CreateMemberController {


    @Autowired
    private AccountService accountService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberService memberService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping("/members")
    public String addMemberForm(Model model, HttpSession session) {
        List<Account> accounts = accountService.getAllAccounts();

        model.addAttribute("account", new Account());
        model.addAttribute("member", new MemberDTO());
        model.addAttribute("accountDTO", new AccountDTO());

        List<String> allUsernames = accounts != null ? accounts.stream().map(Account::getUsername).toList() : List.of();
        List<String> allEmails = accounts != null ? accounts.stream().map(Account::getEmail).toList() : List.of();
        List<String> allIdentityCards = accounts != null ? accounts.stream().map(Account::getIdentityCard).toList() : List.of();
        List<String> allPhoneNumbers = accounts != null ? accounts.stream().map(Account::getPhoneNumber).toList() : List.of();

        model.addAttribute("existingUsernames", allUsernames);
        model.addAttribute("existingEmails", allEmails);
        model.addAttribute("existingIdentityCards", allIdentityCards);
        model.addAttribute("existingPhoneNumbers", allPhoneNumbers);
        session.setAttribute("lastUsernames", allUsernames);
        session.setAttribute("lastEmails", allEmails);
        session.setAttribute("lastIdentityCards", allIdentityCards);
        session.setAttribute("lastPhoneNumbers", allPhoneNumbers);

        return "employee/add-member";
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            password.append(chars.charAt(randomIndex));
        }
        return password.toString();
    }

    private void sendWelcomeEmail(String toEmail, String username, String rawPassword) throws MessagingException, MessagingException, UnsupportedEncodingException {
        String subject = "Tài khoản thành viên mới tại MoonCinema";

        // Tạo context và set biến cho Thymeleaf
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("password", rawPassword);

        // Render template
        String content = templateEngine.process("/newMember", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setFrom("noreply@top1cinema.vn", "MoonCinema");
        helper.setText(content, true);

        mailSender.send(message);
    }
    @PostMapping("/add")
    public String addMember(@Valid @ModelAttribute("account") Account account, BindingResult accountBindingResult,
                            @Valid @ModelAttribute("member") Member member, BindingResult memberBindingResult,
                            Model model,
                            @Valid @ModelAttribute("accountDTO") AccountDTO accountDTO,
                            BindingResult accountDTOBindingResult,
                            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
                            @RequestParam(value = "useDefaultAvatar", required = false) String useDefaultAvatar,
                            RedirectAttributes redirectAttributes) {
        if(accountBindingResult.hasErrors() || memberBindingResult.hasErrors() || accountDTOBindingResult.hasErrors()) {
            System.out.println("---- VALIDATION ERROR -----");

            if (accountBindingResult.hasErrors()) {
                System.out.println("Account Errors:");
                accountBindingResult.getFieldErrors().forEach(err ->
                        System.out.println("Field: " + err.getField() + " - " + err.getDefaultMessage()));
            }

            if (memberBindingResult.hasErrors()) {
                System.out.println("Member Errors:");
                memberBindingResult.getFieldErrors().forEach(err ->
                        System.out.println("Field: " + err.getField() + " - " + err.getDefaultMessage()));
            }

            if (accountDTOBindingResult.hasErrors()) {
                System.out.println("AccountDTO Errors:");
                accountDTOBindingResult.getFieldErrors().forEach(err ->
                        System.out.println("Field: " + err.getField() + " - " + err.getDefaultMessage()));
            }

            model.addAttribute("account", account);
            model.addAttribute("member", member);
            model.addAttribute("accountDTO", accountDTO);
            model.addAttribute("error", "Vui lòng kiểm tra lại thông tin nhập vào.");
            return "employee/add-member";
        }

        try {
            accountDTO.trimFields();

            // ✅ Nếu user bấm nút "Xóa ảnh" → dùng ảnh mặc định
            if ("true".equals(useDefaultAvatar)) {
                account.setAvatarUrl("https://res.cloudinary.com/dycfyoh8r/image/upload/v1752562591/f817d8af-14c5-4313-b349-8cb8eab0306b.png");

                // ✅ Nếu có chọn ảnh mới → lưu và cập nhật avatar
            } else if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = cloudinaryService.uploadAvatarMember(avatarFile, account.getUsername(), "member_avatars");
                account.setAvatarUrl(avatarUrl);

                // ✅ Ngược lại: không xóa và không chọn ảnh mới → giữ avatar hiện tại
            } else {
                // Trường hợp không chọn ảnh và cũng không bấm "xóa" (để nguyên preview default)
                account.setAvatarUrl("https://res.cloudinary.com/dycfyoh8r/image/upload/v1752562591/f817d8af-14c5-4313-b349-8cb8eab0306b.png");
            }
            String rawPassword = generateRandomPassword();
            account.setPassword(passwordEncoder.encode(rawPassword));
            memberService.addMember(account, member, accountDTO);
            sendWelcomeEmail(account.getEmail(), account.getUsername(), rawPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm thành viên " + member.getAccount().getFullName() + " thành công!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "employee/add-member";
        }

        return "redirect:/employee/members";
    }
}
