package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.model.Notification;
import movie_theater_gr4.project_gr4.security.CustomUserDetails;
import movie_theater_gr4.project_gr4.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationController(NotificationService notificationService, SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping({"/n"})
    public String showNotifications(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        int accountId = ((CustomUserDetails) authentication.getPrincipal()).getAccountId();
        model.addAttribute("notifications", notificationService.getNotificationByAccountId(accountId));
        model.addAttribute("accountId", accountId);
        return "notification";
    }

    @MessageMapping("/sendNotification/{accountId}")
    public void sendNotification(@DestinationVariable int accountId, @Payload Notification notification) {
        try {
            int authenticatedAccountId = getAuthenticatedAccountId();
            if (notification.getAccount() == null) {
                notification.setAccount(new movie_theater_gr4.project_gr4.model.Account());
                notification.getAccount().setAccountId(authenticatedAccountId);
            }
            logger.debug("Sending notification for accountId: {}, title: {}", authenticatedAccountId, notification.getTitle());
            notificationService.sendAndSaveNotification(authenticatedAccountId, notification.getTitle(), notification.getMessage());
            messagingTemplate.convertAndSend("/topic/notifications/" + accountId, notification);
        } catch (Exception e) {
            logger.error("Error processing WebSocket notification: {}", e.getMessage());
        }
    }

    @PostMapping("/send")
    public String handleNotificationForm(@RequestParam("title") String title,
                                         @RequestParam("message") String message,
                                         Model model) {
        try {
            int accountId = getAuthenticatedAccountId();
            logger.debug("Processing form submission for accountId: {}, title: {}", accountId, title);
            notificationService.sendAndSaveNotification(accountId, title, message);
            model.addAttribute("successMessage", "Notification sent successfully");
        } catch (Exception e) {
            logger.error("Error processing form submission: {}", e.getMessage());
            model.addAttribute("errorMessage", "Failed to send notification");
        }
        return "notification";
    }

    @GetMapping
    @ResponseBody
    public List<Notification> getNotificationsByAccountId() {
        int accountId = getAuthenticatedAccountId();
        logger.debug("Fetching notifications for accountId: {}", accountId);
        return notificationService.getNotificationByAccountId(accountId);
    }

    @GetMapping("/form")
    public String showNotificationForm(Model model) {
        try {
            int accountId = getAuthenticatedAccountId();
            logger.debug("Displaying notification form for accountId: {}", accountId);
            model.addAttribute("accountId", accountId);
            return "notification";
        } catch (IllegalStateException e) {
            logger.error("Error retrieving accountId: {}", e.getMessage());
            return "redirect:/auth";
        }
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public void markAllNotificationsAsRead() {
        int accountId = getAuthenticatedAccountId();
        logger.debug("Marking all notifications as read for accountId: {}", accountId);
        notificationService.markAllAsRead(accountId);
    }

    @PostMapping("/mark-read")
    @ResponseBody
    public void markOneNotificationAsRead(@RequestParam("notificationId") int notificationId) {
        try {
            int accountId = getAuthenticatedAccountId();
            logger.debug("Marking notification {} as read for accountId: {}", notificationId, accountId);
            notificationService.markOneNotificationAsRead(accountId, notificationId);
        } catch (Exception e) {
            logger.error("Error marking notification {} as read for accountId: {}: {}", notificationId, e.getMessage());
            throw new IllegalStateException("Failed to mark notification as read");
        }
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public int getUnreadCount() {
        int accountId = getAuthenticatedAccountId();
        logger.debug("Fetching unread count for accountId: {}", accountId);
        return notificationService.getUnreadCountByAccountId(accountId);
    }

    @PostMapping("/delete-notification")
    @ResponseBody
    public void deleteOneNotificationAsRead(@RequestParam("notificationId") int notificationId) {
        try {
            int accountId = getAuthenticatedAccountId();
            logger.debug("Marking notification {} as read for accountId: {}", notificationId, accountId);
            notificationService.deleteNotification(accountId, notificationId);
        } catch (Exception e) {
            logger.error("Error marking notification {} as read for accountId: {}: {}", notificationId, e.getMessage());
            throw new IllegalStateException("Failed to mark notification as read");
        }
    }

    private int getAuthenticatedAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.error("No authenticated user found");
            throw new IllegalStateException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            int accountId = userDetails.getAccountId();
            if (accountId <= 0) {
                logger.error("Invalid accountId: {} for user: {}", accountId, userDetails.getUsername());
                throw new IllegalStateException("Invalid accountId");
            }
            return accountId;
        }

        logger.error("Principal is not of type CustomUserDetails: {}", principal.getClass().getName());
        throw new IllegalStateException("Unable to retrieve accountId from authenticated user");
    }
}