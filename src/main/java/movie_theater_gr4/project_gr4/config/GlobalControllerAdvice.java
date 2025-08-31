package movie_theater_gr4.project_gr4.config;

import movie_theater_gr4.project_gr4.security.CustomUserDetails;
import movie_theater_gr4.project_gr4.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    @Autowired
    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            int accountId = userDetails.getAccountId();
            model.addAttribute("accountId", accountId);

            model.addAttribute("countUnRead", notificationService.getUnreadCountByAccountId(accountId));
            model.addAttribute("notifications", notificationService.getNotificationByAccountId(accountId));
        } else {
            model.addAttribute("notifications", Collections.emptyList()); // Provide empty list for non-authenticated users
        }
    }
}