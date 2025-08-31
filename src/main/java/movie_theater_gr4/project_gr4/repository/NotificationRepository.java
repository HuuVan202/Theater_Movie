package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Query("SELECT n FROM Notification n WHERE n.account.accountId = :accountId ORDER BY n.notification_Id DESC")
    List<Notification> findNotificationByAccountId(@Param("accountId") int accountId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.account.accountId = :accountId AND n.status = false")
    int countByAccountAccountIdAndStatus(@Param("accountId") int accountId);
}
