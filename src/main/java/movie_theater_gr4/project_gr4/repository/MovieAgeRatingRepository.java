package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.MovieAgeRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MovieAgeRatingRepository extends JpaRepository<MovieAgeRating,Long> {

    @Query(value = "SELECT mar.* FROM movie_age_rating AS mar JOIN movie AS m ON m.rating_code = mar.rating_code  WHERE m.movie_id = :movieId ", nativeQuery = true)
    MovieAgeRating findAgeRatingByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT mar FROM MovieAgeRating mar WHERE mar.ratingCode = :ratingCode")
    MovieAgeRating findAgeRatingByRatingCode(@Param("ratingCode") String ratingCode);
    MovieAgeRating findByRatingCode(String ratingCode);
}
