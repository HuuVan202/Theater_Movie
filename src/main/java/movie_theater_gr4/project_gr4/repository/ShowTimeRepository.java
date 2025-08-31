package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Schedule;
import movie_theater_gr4.project_gr4.model.ShowDate;
import movie_theater_gr4.project_gr4.model.Showtime;
import movie_theater_gr4.project_gr4.model.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ShowTimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findDistinctShowtimesByMovie_MovieId(@Param("movieId") Long movieId);

    @Query("SELECT DISTINCT st.version FROM Showtime st " +
            "WHERE st.movie.movieId = :movieId AND st.showDate.showDate = :date")
    List<Version> findDistinctVersionsByMovieIdAndDate(@Param("movieId") Long movieId,
                                                       @Param("date") LocalDate date);

    @Query("SELECT st FROM Showtime st " +
            "JOIN FETCH st.schedule sch " +
            "WHERE st.movie.movieId = :movieId " +
            "AND st.showDate.showDate = :date " +
            "AND st.version.versionId = :versionId")
    List<Showtime> findByMovieIdAndShowDateAndVersionId(
            @Param("movieId") Long movieId,
            @Param("date") LocalDate date,
            @Param("versionId") Long versionId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Showtime s " +
            "WHERE s.movie.movieId = :movieId AND s.showDate.showDate >= :currentDate")
    boolean existsByMovieIdAndShowDateGreaterThanOrEqual(@Param("movieId") Long movieId,
                                                         @Param("currentDate") LocalDate currentDate);

    @Query("SELECT s FROM Showtime s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.room " +
            "JOIN FETCH s.version " +
            "JOIN FETCH s.showDate " +
            "JOIN FETCH s.schedule " +
            "WHERE s.showDate.showDate = :showDate")
    List<Showtime> findByShowDate(@Param("showDate") LocalDate showDate);

    @Query("SELECT s FROM Showtime s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.room " +
            "JOIN FETCH s.version " +
            "JOIN FETCH s.showDate " +
            "JOIN FETCH s.schedule " +
            "WHERE s.showDate.showDate BETWEEN :startDate AND :endDate")
    List<Showtime> findByDateRange(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Showtime s " +
            "WHERE s.room.roomId = :roomId AND s.showDate.showDate = :showDate")
    boolean existsByRoomIdAndShowDate(@Param("roomId") Long roomId, @Param("showDate") LocalDate showDate);

    @Query(value = "SELECT suggest_next_showtime(:roomId, :showDateId, :movieId)", nativeQuery = true)
    LocalTime suggestNextShowtime(@Param("roomId") Long roomId,
                                  @Param("showDateId") Long showDateId,
                                  @Param("movieId") Long movieId);

    @Query(value = "SELECT cr.room_id, cr.room_name, " +
            "COUNT(DISTINCT st.movie_id) AS movie_count, " +
            "JSON_AGG(" +
            "    JSON_BUILD_OBJECT(" +
            "        'movie_id', m.movie_id, " +
            "        'movie_name', m.movie_name, " +
            "        'showtime_count', (" +
            "            SELECT COUNT(*) " +
            "            FROM public.showtime st2 " +
            "            WHERE st2.movie_id = m.movie_id " +
            "            AND st2.room_id = cr.room_id " +
            "            AND st2.show_date_id = sd.show_date_id" +
            "        )," +
            "        'showtimes', (" +
            "            SELECT JSON_AGG(" +
            "                JSON_BUILD_OBJECT(" +
            "                    'schedule_time', s2.schedule_time," +
            "                    'version_name', v.version_name" +
            "                )" +
            "            )" +
            "            FROM public.showtime st3 " +
            "            JOIN public.schedule s2 ON st3.schedule_id = s2.schedule_id " +
            "            JOIN public.version v ON st3.version_id = v.version_id " +
            "            WHERE st3.movie_id = m.movie_id " +
            "            AND st3.room_id = cr.room_id " +
            "            AND st3.show_date_id = sd.show_date_id" +
            "        )" +
            "    )" +
            ") FILTER (WHERE m.movie_id IS NOT NULL) AS movies " +
            "FROM public.show_dates sd " +
            "LEFT JOIN public.showtime st ON sd.show_date_id = st.show_date_id " +
            "LEFT JOIN public.cinema_room cr ON st.room_id = cr.room_id " +
            "LEFT JOIN public.movie m ON st.movie_id = m.movie_id " +
            "WHERE sd.show_date = :showDate " +
            "GROUP BY sd.show_date, cr.room_id, cr.room_name " +
            "HAVING COUNT(st.showtime_id) > 0", nativeQuery = true)
    List<Map<String, Object>> getRoomStatisticsByDate(@Param("showDate") LocalDate showDate);

    @Query("SELECT s FROM Showtime s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.room " +
            "JOIN FETCH s.version " +
            "JOIN FETCH s.showDate " +
            "JOIN FETCH s.schedule")
    List<Showtime> findAllWithDetails();

    @Query("SELECT s FROM Showtime s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.room " +
            "JOIN FETCH s.version " +
            "JOIN FETCH s.showDate " +
            "JOIN FETCH s.schedule " +
            "WHERE s.showDate.showDate BETWEEN :startDate AND :endDate")
    List<Showtime> findByShowDate_ShowDateBetween(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end);

    @Query(value = "SELECT m.movie_id, m.movie_name, COUNT(st.showtime_id) as showtime_count " +
            "FROM public.showtime st " +
            "JOIN public.movie m ON st.movie_id = m.movie_id " +
            "JOIN public.show_dates sd ON st.show_date_id = sd.show_date_id " +
            "WHERE sd.show_date = :selectedDate " +
            "GROUP BY m.movie_id, m.movie_name " +
            "HAVING COUNT(st.showtime_id) > 0 " +
            "ORDER BY showtime_count DESC", nativeQuery = true)
    List<Map<String, Object>> getMovieShowtimeStats(@Param("selectedDate") LocalDate selectedDate);

//    @Query(value = "SELECT COUNT(*) FROM public.showtime s " +
//            "JOIN public.cinema_room c ON s.room_id = c.room_id " +
//            "WHERE c.room_id = :roomId", nativeQuery = true)
//    long countByRoomId(Long roomId);

    @Query(value = "SELECT COUNT(*) FROM public.showtime s " +
            "JOIN public.show_dates d ON s.show_date_id = d.show_date_id " +
            "WHERE s.room_id = :roomId " +
            "AND d.show_date >= CURRENT_DATE", nativeQuery = true)
    long countByRoomId(Long roomId);

    @Query(value = "SELECT m.movie_id, m.movie_name, COUNT(st.showtime_id) as showtime_count " +
            "FROM public.showtime st " +
            "JOIN public.movie m ON st.movie_id = m.movie_id " +
            "JOIN public.show_dates sd ON st.show_date_id = sd.show_date_id " +
            "WHERE sd.show_date BETWEEN :startDate AND :endDate " +
            "GROUP BY m.movie_id, m.movie_name " +
            "HAVING COUNT(st.showtime_id) > 0 " +
            "ORDER BY showtime_count DESC", nativeQuery = true)
    List<Map<String, Object>> getMovieShowtimeStatsByRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM Showtime s " +
            "JOIN FETCH s.movie " +
            "JOIN FETCH s.room " +
            "JOIN FETCH s.version " +
            "JOIN FETCH s.showDate " +
            "JOIN FETCH s.schedule " +
            "WHERE s.showDate.showDate = :showDate AND s.room.roomId = :roomId")
    List<Showtime> findByShowDateAndRoomId(@Param("showDate") LocalDate showDate, @Param("roomId") Long roomId);
}
