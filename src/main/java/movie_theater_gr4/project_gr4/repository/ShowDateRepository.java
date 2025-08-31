package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.ShowDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowDateRepository extends JpaRepository<ShowDate, Long> {

    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.movieId = :movieId")
    List<ShowDate> findDistinctShowDatesByMovieId(@Param("movieId") Long movieId);


    Optional<ShowDate> findByShowDate(LocalDate showDate);

    ShowDate findTopByOrderByShowDateIdDesc();
}
