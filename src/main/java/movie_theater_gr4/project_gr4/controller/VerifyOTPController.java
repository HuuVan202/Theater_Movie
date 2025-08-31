package movie_theater_gr4.project_gr4.controller;

import jakarta.servlet.http.HttpSession;
import movie_theater_gr4.project_gr4.model.OTPData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class VerifyOTPController {

    private static final long OTP_EXPIRY_DURATION_MS = 5 * 60 * 1000;
    //Để in ra debug thôi
    private static final Logger logger = LoggerFactory.getLogger(VerifyOTPController.class);

    @GetMapping("/verifyOTP")
    public String display(Model model, HttpSession session) {
        String email = (String) session.getAttribute("email");

        if (email == null) {
            session.setAttribute("errorEmail", "Vui lòng nhập Email cần khôi phục.");
            return "redirect:/forgot";
        }

        //Check khi người dùng không nhập OTP mà vô thẳng ChangePassword
        if (session.getAttribute("errorOTP") != null) {
            model.addAttribute("error", session.getAttribute("errorOTP"));
        }

        //để có mail mà gửi OTP lại
        model.addAttribute("email", email);

        //để xóa báo lỗi trên session
        session.removeAttribute("errorOTP");
        return "verify_OTP";
    }

    @PostMapping("/verifyOTP")
    public String verifyOTP(
            @RequestParam("OTPinput") String otp,
            HttpSession session,
            Model model) {
        otp = otp.trim();
        String token = (String) session.getAttribute("otpToken");
        logger.info("token: {}", token);


        // Kiểm tra khi nào token hết session
        if (token == null) {
            model.addAttribute("error", "Token or OTP expired session.");
            return "verify_OTP";
        }

        // Lấy OTP ra từ Map tĩnh ở OTPData
        OTPData otpData = OTPData.getOtpStore().get(token);

        // Nếu không có OTPData thì xóa token và số lần thử
        if (otpData == null) {
//        model.addAttribute("email", session.getAttribute("email"));
            model.addAttribute("error", "OTP session expired or invalid.");
            session.removeAttribute("otpToken");
            session.removeAttribute("otpAttempts");
            return "verify_OTP";
        }

        // Lấy số lần thử từ session, mặc định là MAX_ATTEMPTS nếu chưa có
        Integer attempts = (Integer) session.getAttribute("otpAttempts");

        // Kiểm tra OTP
        if (!otpData.getOtp().equals(otp)) {
            attempts--;
            session.setAttribute("otpAttempts", attempts);
            if (attempts <= 0) {
                OTPData.getOtpStore().remove(token);
                session.removeAttribute("otpToken");
                session.removeAttribute("otpAttempts");
                // gửi lại mail để khi gửi lại mail thì co địa chỉ
                model.addAttribute("email", session.getAttribute("email"));
                model.addAttribute("error", "Too many invalid attempts. Please request a new OTP.");
//                return "redirect:/forgot";
                return "verify_OTP";
            }
            // gửi lại mail để khi gửi lại mail thì co địa chỉ
            model.addAttribute("email", session.getAttribute("email"));
            model.addAttribute("error", "Invalid OTP. You have " + attempts + " attempt left");
            return "verify_OTP";
        }

        // Kiểm tra thời gian hết hạn OTP
        if (System.currentTimeMillis() - otpData.getTimestamp() > OTP_EXPIRY_DURATION_MS) {
            OTPData.getOtpStore().remove(token);
            session.removeAttribute("otpToken");
            session.removeAttribute("otpAttempts");
            // gửi lại mail để khi gửi lại mail thì co địa chỉ
            model.addAttribute("email", session.getAttribute("email"));
            model.addAttribute("error", "OTP has expired.");
            return "redirect:/forgot";
        }

        // OTP verified, clean up
        OTPData.getOtpStore().remove(token);
        session.removeAttribute("otpToken");
        session.removeAttribute("otpAttempts");
        // Dùng để check xem người dùng nhập OTP chưa
        session.setAttribute("otp", otp);
        return "redirect:/changePassword";
    }

}
