package movie_theater_gr4.project_gr4.repository;

import jakarta.transaction.Transactional;
import movie_theater_gr4.project_gr4.model.MoviePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MoviePromotionRepository extends JpaRepository<MoviePromotion, Long> {
    @Query("SELECT mp FROM MoviePromotion mp WHERE mp.promotionId = :promotionId")
    MoviePromotion findByPromotionId(@Param("promotionId") Integer promotionId);

    @Transactional
    @Modifying
    @Query("DELETE FROM MoviePromotion mp WHERE mp.promotionId = :promotionId")
    void deleteByPromotionId(@Param("promotionId") Integer promotionId);
}