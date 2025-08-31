package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    @Query("SELECT p FROM Promotion p WHERE p.startTime <= :currentTime AND p.endTime >= :currentTime AND p.active = true")
    List<Promotion> findByStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT p FROM Promotion p WHERE p.active = true and p.startTime <= :currentTime ORDER BY p.startTime DESC, p.discountLevel DESC")
    List<Promotion> findLatestPromotions(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT p FROM Promotion p WHERE p.active = true AND (:dayOfWeek IS NULL OR p.dayOfWeek = :dayOfWeek) AND p.startTime <= :currentTime AND p.endTime >= :currentTime")
    List<Promotion> findActivePromotionsByDayOfWeek(
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT p FROM Promotion p WHERE p.ticketTypeId = :ticketTypeId AND p.active = true AND p.startTime <= :currentTime AND p.endTime >= :currentTime")
    List<Promotion> findActivePromotionsByTicketType(
            @Param("ticketTypeId") Integer ticketTypeId,
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT p FROM Promotion p JOIN MoviePromotion mp ON p.promotionId = mp.promotionId " +
            "WHERE mp.movieId = :movieId AND p.active = true AND p.startTime <= :currentTime AND p.endTime >= :currentTime")
    List<Promotion> findActivePromotionsByMovie(@Param("movieId") Long movieId, @Param("currentTime") LocalDateTime currentTime);
}
