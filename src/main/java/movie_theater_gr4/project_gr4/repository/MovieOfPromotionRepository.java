package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.MovieOfPromotion;
import movie_theater_gr4.project_gr4.model.MovieOfPromotionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieOfPromotionRepository extends JpaRepository<MovieOfPromotion, MovieOfPromotionId> {
    List<MovieOfPromotion> findByMovie_MovieId(Long movieId);
}