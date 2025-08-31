    package movie_theater_gr4.project_gr4.service;

    import movie_theater_gr4.project_gr4.model.Account;
    import movie_theater_gr4.project_gr4.model.Notification;
    import movie_theater_gr4.project_gr4.repository.NotificationRepository;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class NotificationService {

        private final NotificationRepository notificationRepository;
        private final SimpMessagingTemplate messagingTemplate;
        private final AccountService accountService;

        public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate, AccountService accountService) {
            this.notificationRepository = notificationRepository;
            this.messagingTemplate = messagingTemplate;
            this.accountService = accountService;
        }

        public List<Notification> getNotificationByAccountId(int accountId) {
            return notificationRepository.findNotificationByAccountId(accountId);
        }

        public int getUnreadCountByAccountId(int accountId) {
            return notificationRepository.countByAccountAccountIdAndStatus(accountId);
        }

        public void sendAndSaveNotification(int accountId, String title, String message) {
            Optional<Account> accountOptional = accountService.findAccountByIdNotifition(accountId);
            if (accountOptional.isEmpty()) {
                throw new IllegalArgumentException("Account not found for id: " + accountId);
            }

            Notification notification = Notification.builder()
                    .account(accountOptional.get())
                    .title(title)
                    .message(message)
                    .date(LocalDateTime.now())
                    .status(false)
                    .build();
            notificationRepository.save(notification);

            int unreadCount = getUnreadCountByAccountId(accountId);
            messagingTemplate.convertAndSend("/topic/notifications/" + accountId, notification);
            messagingTemplate.convertAndSend("/topic/unread-count/" + accountId, unreadCount);

        }

        public void markAllAsRead(int accountId) {
            List<Notification> notifications = notificationRepository.findNotificationByAccountId(accountId);
            notifications.forEach(notification -> {
                if (!notification.getStatus()) {
                    notification.setStatus(true);
                    notificationRepository.save(notification);
                }
            });
            // Send updated unread count to WebSocket topic
            int unreadCount = getUnreadCountByAccountId(accountId);
            messagingTemplate.convertAndSend("/topic/unread-count/" + accountId, unreadCount);
        }

        public void markOneNotificationAsRead(int accountId, int notificationId) {
            List<Notification> notifications = notificationRepository.findNotificationByAccountId(accountId);
            notifications.forEach(notification -> {
                if (notification.getNotification_Id() == notificationId) {
                    notification.setStatus(true);
                    notificationRepository.save(notification);
                    return;
                }
            });
            // Send updated unread count to WebSocket topic
            int unreadCount = getUnreadCountByAccountId(accountId);
            messagingTemplate.convertAndSend("/topic/unread-count/" + accountId, unreadCount);
        }

        public void deleteNotification(int accountId, int notificationId) {
            List<Notification> notifications = notificationRepository.findNotificationByAccountId(accountId);
            notifications.forEach(notification -> {
                if (notification.getNotification_Id() == notificationId) {
                    notificationRepository.delete(notification);
                    // Broadcast deletion event
                    messagingTemplate.convertAndSend("/topic/notifications/" + accountId,
                            new  NotificationDeletionEvent(notificationId));
                    return;
                }
            });
            int unreadCount = getUnreadCountByAccountId(accountId);
            messagingTemplate.convertAndSend("/topic/unread-count/" + accountId, unreadCount);
        }

        // New class to represent a deletion event
        public static class NotificationDeletionEvent {
            private final int notificationId;

            public NotificationDeletionEvent(int notificationId) {
                this.notificationId = notificationId;
            }

            public int getNotificationId() {
                return notificationId;
            }
        }
    }