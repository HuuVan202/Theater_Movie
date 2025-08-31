package movie_theater_gr4.project_gr4.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDTO;
import movie_theater_gr4.project_gr4.bookingMember.dto.InvoiceDetailsDTO;
import movie_theater_gr4.project_gr4.dto.AccountProflieDTO;
import movie_theater_gr4.project_gr4.employee.dto.BookingInfoDTO;
import movie_theater_gr4.project_gr4.employee.service.SelectMovieService;
import movie_theater_gr4.project_gr4.mapper.AccountMapper;
import movie_theater_gr4.project_gr4.model.Account;
import movie_theater_gr4.project_gr4.service.AccountService;
import movie_theater_gr4.project_gr4.service.BookingListService;
import movie_theater_gr4.project_gr4.service.CloudinaryService;
import movie_theater_gr4.project_gr4.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import movie_theater_gr4.project_gr4.security.CustomUserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.context.MessageSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    @Autowired
    BookingListService bookingListService;
    @Autowired
    SelectMovieService selectMovieService;


    AccountService accountService;
    AccountMapper accountMapper;
    NotificationService notificationService;
    CloudinaryService cloudinaryService;
    private final MessageSource messageSource;
    //path để lưu ở vào folder
    private static final String UPLOAD_DIR = "src/main/resources/static/avatars/";
    //path để hiện thị ảnh từ folder
    private static final String DISPLAY_DIR = "/avatars/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public ProfileController(AccountService accountService, AccountMapper accountMapper, CloudinaryService cloudinaryService,
                             MessageSource messageSource, NotificationService notificationService) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.cloudinaryService = cloudinaryService;
        this.messageSource = messageSource;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        //Get account from session
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Account account = accountService.findAccountByUsername(username);
        CustomUserDetails updatedUserDetails = new CustomUserDetails(account); // Create new CustomUserDetails
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                null, // Credentials are typically null after authentication
                updatedUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        AccountProflieDTO accountProflieDTO = accountMapper.toAccountProflieDTO(accountService.findAccountByUsername(username));
        String keyword = accountProflieDTO.getIsGoogle() == null
                ? accountProflieDTO.getUsername()
                : accountProflieDTO.getEmail();
        List<InvoiceDTO> bookingList = bookingListService.getAllInvoiceByKeyword(keyword);
        List<Map<String, Object>> combinedBookings = new ArrayList<>();

        for (InvoiceDTO booking : bookingList) {
            Map<String, Object> combined = new HashMap<>();
            combined.put("booking", booking);

            // Lấy bookingInfo, nếu không có thì để null
            Object bookingInfo = selectMovieService.findBookingInfoByInvoiceId(booking.getInvoiceId());
            combined.put("bookingInfo", bookingInfo != null ? bookingInfo : null);

            combinedBookings.add(combined);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gson.toJson(combinedBookings));
        String combinedBookingsJson = gson.toJson(combinedBookings);

        // Gọi service mới thay cho xử lý thủ công
        List<InvoiceDetailsDTO> fullBookingList = bookingListService.getFullInvoiceByKeyword(keyword);

        model.addAttribute("combinedBookingsJson", combinedBookingsJson);
        model.addAttribute("bookingList", bookingList);
        model.addAttribute("fullBookingList", fullBookingList);
        model.addAttribute("accountProfile", accountProflieDTO);
        return "userProfile";
    }

    @PostMapping("/profile/update")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody AccountProflieDTO accountProfileDTO,
                                                             BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        // Custom validation for email uniqueness
        if (accountService.isExistingEmail(accountProfileDTO.getEmail()) &&
                !accountService.findAccountByUsername(accountProfileDTO.getUsername()).getEmail()
                        .equals(accountProfileDTO.getEmail())) {
            response.put("success", false);
            response.put("message", "Email is already in use");
            return ResponseEntity.badRequest().body(response);
        }

        if (bindingResult.hasErrors()) {
            response.put("success", false);
            response.put("message", bindingResult.getFieldErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            accountService.updateAccount(accountProfileDTO);
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update profile");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @AuthenticationPrincipal CustomUserDetails accountCurrent,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword) {

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Map<String, Object> response = new HashMap<>();

        //Check xem có trùng mật khẩu cũ không
        boolean checkCurrentPassword = passwordEncoder.matches(currentPassword, accountCurrent.getPassword());
        //Check xem mật khẩu có đúng với định dạng chưa
        boolean checkFormPassword = !newPassword.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$");
        //Check xem có trùng mật khẩu không
        boolean checkNewPassword = newPassword.equals(confirmPassword);


        if (!checkCurrentPassword) {
            response.put("success", false);
            response.put("message", "Mật khẩu không khớp với mật khẩu cũ!");
            return ResponseEntity.badRequest().body(response);
        }

        if (checkFormPassword) {
            response.put("message", "Mật khẩu chưa mạnh.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!checkNewPassword) {
            response.put("success", false);
            response.put("message", "Mật khẩu không trùng khớp với nhau.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Account account = accountService.findAccountByUsername(accountCurrent.getUsername());
            account.setPassword(passwordEncoder.encode(newPassword));
            accountService.updateAccount(account);

            CustomUserDetails updatedUserDetails = new CustomUserDetails(account); // Create new CustomUserDetails
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    null, // Credentials are typically null after authentication
                    updatedUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            response.put("success", true);
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Password to update profile");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/profile/upload-image")
    public String uploadAvatar(
            @RequestParam("image") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "avatars") String folder,
            RedirectAttributes redirectAttributes, Locale locale) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("showPopup", true);
                redirectAttributes.addFlashAttribute("uploadSuccess", false);
                redirectAttributes.addFlashAttribute("uploadMessage", messageSource.getMessage("profile.avatar.select_image", null, locale));
                return "redirect:/profile";
            }

            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                redirectAttributes.addFlashAttribute("showPopup", true);
                redirectAttributes.addFlashAttribute("uploadSuccess", false);
                redirectAttributes.addFlashAttribute("uploadMessage",
                        messageSource.getMessage("profile.avatar.file_too_large",
                                new Object[]{"5MB"}, locale));
                return "redirect:/profile";
            }

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Account updatedAccount = cloudinaryService.uploadAvatar(file, username, folder);
            redirectAttributes.addFlashAttribute("showPopup", true);
            redirectAttributes.addFlashAttribute("uploadSuccess", true);
            redirectAttributes.addFlashAttribute("uploadMessage", messageSource.getMessage("profile.avatar.upload_success", null, locale));
            redirectAttributes.addFlashAttribute("avatarUrl", updatedAccount.getAvatarUrl());

            int accountId = ((CustomUserDetails) authentication.getPrincipal()).getAccountId();
            notificationService.sendAndSaveNotification(accountId, "Cập Nhật Ảnh Thành Công", "Bạn đã cập nhật ảnh đại diện thành công!");

            return "redirect:/profile";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("showPopup", true);
            redirectAttributes.addFlashAttribute("uploadSuccess", false);
            redirectAttributes.addFlashAttribute("uploadMessage", messageSource.getMessage("profile.avatar.upload_fail", new Object[]{e.getMessage()}, locale));
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("showPopup", true);
            redirectAttributes.addFlashAttribute("uploadSuccess", false);
            redirectAttributes.addFlashAttribute("uploadMessage", messageSource.getMessage("profile.avatar.unknown_error", new Object[]{e.getMessage()}, locale));
            return "redirect:/profile";
        }
    }
}
