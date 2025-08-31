package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Schedule;
import movie_theater_gr4.project_gr4.model.ShowDate;
import movie_theater_gr4.project_gr4.model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT DISTINCT s.schedule FROM Showtime s WHERE s.movie.movieId = :movieId")
    List<Schedule> findDistinctScheduleByMovieId(@Param("movieId") Long movieId);

    @Query("SELECT st FROM Showtime st JOIN FETCH st.schedule WHERE st.movie.movieId = :movieId")
    List<Showtime> findByMovieIdWithSchedule(@Param("movieId") Long movieId);

//    List<Schedule> findSchedulesBy(String scheduleTime);
    Optional<Schedule> findByScheduleTime(LocalTime scheduleTime);
}
