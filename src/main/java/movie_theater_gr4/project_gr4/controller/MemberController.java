package movie_theater_gr4.project_gr4.controller;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import movie_theater_gr4.project_gr4.dto.AccountDTO;
import movie_theater_gr4.project_gr4.dto.MemberDTO;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.model.Member;
import movie_theater_gr4.project_gr4.repository.MemberRepository;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.CloudinaryService;
import movie_theater_gr4.project_gr4.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/members")
public class MemberController {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping
    public String viewMembers(@RequestParam(defaultValue = "0") int page, Model model,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) String direction) {
        String sortField = (sortBy != null) ? sortBy : "memberId";
        String sortDirection = (direction != null) ? direction : "desc";

        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, 5, sort); // 10 bản ghi mỗi trang

        Page<MemberDTO> memberPage = memberService.searchMembers(keyword, pageable);

        List<Long> memberIds = memberPage.getContent().stream()
                .map(MemberDTO::getMemberId)
                .map(Integer::longValue)
                .collect(Collectors.toList());
//        System.out.println("size=================" + memberIds.size());

        List<Map<String, Object>> statusList = memberRepository.findStatusByMemberIds(memberIds);

        List<Map<String, Object>> memberList = memberPage.getContent().stream().map(dto -> {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put("member", dto);

            String statusText = "Unknown";
            for (Map<String, Object> statusMap : statusList) {
                if (dto.getMemberId() == ((Number) statusMap.get("memberId")).longValue()) {
                    Integer status = (Integer) statusMap.get("status");
                    statusText = (status != null && status == 1) ? "Active" : (status != null && status == 0) ? "Inactive" : "Unknown";
                    break;
                }
            }
            memberMap.put("status", statusText);
            return memberMap;
        }).collect(Collectors.toList());

        model.addAttribute("sortBy", sortField);
        model.addAttribute("direction", sortDirection);
        model.addAttribute("members", memberList);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("totalPages", memberPage.getTotalPages());
        return "admin/member/members";
    }

    @GetMapping("/add")
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

        return "admin/add-member";
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
            return "admin/add-member";
        }

        try {
            accountDTO.trimFields();

            // ✅ Nếu user bấm nút "Xóa ảnh" → dùng ảnh mặc định
            if ("true".equals(useDefaultAvatar)) {
                account.setAvatarUrl("http://res.cloudinary.com/dycfyoh8r/image/upload/v1755106434/avatars/avatar_default.jpg");

                // ✅ Nếu có chọn ảnh mới → lưu và cập nhật avatar
            } else if (avatarFile != null && !avatarFile.isEmpty()) {
                String avatarUrl = cloudinaryService.uploadAvatarMember(avatarFile, account.getUsername(), "member_avatars");
                account.setAvatarUrl(avatarUrl);

                // ✅ Ngược lại: không xóa và không chọn ảnh mới → giữ avatar hiện tại
            } else {
                // Trường hợp không chọn ảnh và cũng không bấm "xóa" (để nguyên preview default)
                account.setAvatarUrl("http://res.cloudinary.com/dycfyoh8r/image/upload/v1755106434/avatars/avatar_default.jpg");
            }
            String rawPassword = generateRandomPassword();
            account.setPassword(passwordEncoder.encode(rawPassword));
            memberService.addMember(account, member, accountDTO);
            sendWelcomeEmail(account.getEmail(), account.getUsername(), rawPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm thành viên " + member.getAccount().getFullName() + " thành công!");

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/add-member";
        }

        return "redirect:/admin/members";
    }

    private String processAvatarFile(MultipartFile avatarFile, String username) {
        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Validate file type
                String contentType = avatarFile.getContentType();
                if (!contentType.startsWith("image/")) {
                    throw new RuntimeException("File phải là hình ảnh!");
                }

                // Validate file size (max 5MB)
                if (avatarFile.getSize() > 5 * 1024 * 1024) {
                    throw new RuntimeException("File không được vượt quá 5MB!");
                }

                // Thư mục upload ngoài src/
                String uploadDir = "uploads/employees";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Tạo tên file unique
                String originalFileName = avatarFile.getOriginalFilename();
                String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                String fileName = username + "_" + System.currentTimeMillis() + fileExtension;

                // Lưu file
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                return "/uploads/employees/" + fileName;
            } else {
                return "/img/employees/default-avatar.png";
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + e.getMessage());
        }
    }

    @GetMapping("/detail/{id}")
        public String viewMemberDetail(@PathVariable("id") int memberId, Model model) {
            Member member = memberService.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên"));
            Account account = member.getAccount();

        // Tạo DTO
        AccountDTO accountDTO = AccountDTO.builder()
                .username(account.getUsername())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .phoneNumber(account.getPhoneNumber())
                .identityCard(account.getIdentityCard())
                .address(account.getAddress())
                .dateOfBirth(account.getDateOfBirth() != null ? account.getDateOfBirth() : LocalDate.now())
                .gender(account.getGender())
                .status(account.getStatus())
                .registerDate(account.getRegisterDate())
                .avatarUrl(account.getAvatarUrl())
                .build();

        model.addAttribute("account", account);
        model.addAttribute("member", member);
        model.addAttribute("accountDTO", accountDTO);

        return "admin/member/member-detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteMember(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên"));
            int memberId = member.getMemberId();
            memberService.deleteMember(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vô hiệu hóa thành viên " + member.getAccount().getFullName() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members?page=0";
    }

    @GetMapping("/unlock/{id}")
    public String unlockMember(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            Member member = memberService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thành viên"));
            int memberId = member.getMemberId();
            memberService.unlockMember(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Kích hoạt thành viên " + member.getAccount().getFullName() + " thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members?page=0";
    }

}