package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Movie;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {


    @EntityGraph(attributePaths = {"movieTypes", "movieTypes.type", "ageRating"})
    @Query("SELECT m FROM Movie m WHERE m.movieId = :id")
    Optional<Movie> findMovieWithTypesById(@Param("id") Long id);
    // Tối ưu truy vấn bằng cách chỉ lấy các trường cần thiết cho danh sách phim
    @EntityGraph(attributePaths = {"movieTypes.type", "ageRating"})
    @Query("SELECT m FROM Movie m WHERE m.fromDate <= :currentDate")
    Page<Movie> findNowShowingMovies(@Param("currentDate") LocalDate currentDate, Pageable pageable);


    @EntityGraph(attributePaths = {"movieTypes.type", "ageRating"})
    @Query("SELECT m FROM Movie m WHERE m.fromDate > :currentDate")
    Page<Movie> findComingSoonMovies(@Param("currentDate") LocalDate currentDate, Pageable pageable);


    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.movieTypes mt LEFT JOIN FETCH mt.type " +
            "LEFT JOIN FETCH m.ageRating " +
            "WHERE m.fromDate <= :currentDate AND (m.toDate IS NULL OR m.toDate >= :currentDate)")
    List<Movie> findAllNowShowingMovies(@Param("currentDate") LocalDate currentDate);


    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.versions " +
            "WHERE m.movieId = :movieId")
    Optional<Movie> findMovieWithVersionsById(@Param("movieId") Long movieId);

    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.movieTypes mt " +
            "LEFT JOIN FETCH mt.type " +
            "LEFT JOIN FETCH m.ageRating " +
            "WHERE m.movieId = :movieId")
    Optional<Movie> findMovieWithTypesAndRatingById(@Param("movieId") Long movieId);

    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.showtimes st " +
            "LEFT JOIN FETCH st.showDate " +
            "LEFT JOIN FETCH st.schedule " +
            "WHERE m.movieId = :movieId")
    Optional<Movie> findMovieWithShowtimesById(@Param("movieId") Long movieId);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.movieTypes mt LEFT JOIN FETCH mt.type " +
            "WHERE LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) " +
            "AND m.fromDate > :currentDate")
    Page<Movie> findByMovieNameContainingIgnoreCaseAndFromDateAfter(
            @Param("movieName") String movieName,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.movieTypes mt LEFT JOIN FETCH mt.type " +
            "WHERE LOWER(m.movieName) LIKE LOWER(CONCAT('%', :movieName, '%')) " +
            "AND m.fromDate <= :currentDate AND (m.toDate IS NULL OR m.toDate >= :currentDate)")
    Page<Movie> findByMovieNameContainingIgnoreCaseAndFromDateBeforeAndToDateAfterOrToDateIsNull(
            @Param("movieName") String movieName,
            @Param("currentDate") LocalDate currentDate,
            @Param("currentDate") LocalDate currentDate2,
            Pageable pageable
    );

    @Query("SELECT m FROM Movie m JOIN m.movieTypes mt JOIN mt.type t " +
            "WHERE m.fromDate <= :currentDate AND LOWER(t.typeName) = LOWER(:genre)")
    Page<Movie> findNowShowingMoviesByGenre(
            @Param("currentDate") LocalDate currentDate,
            @Param("genre") String genre,
            Pageable pageable
    );

    @Query("SELECT m FROM Movie m JOIN m.movieTypes mt JOIN mt.type t " +
            "WHERE m.fromDate > :currentDate AND LOWER(t.typeName) = LOWER(:genre)")
    Page<Movie> findComingSoonMoviesByGenre(
            @Param("currentDate") LocalDate currentDate,
            @Param("genre") String genre,
            Pageable pageable
    );

    // Danh sách ID phim đang chiếu (chỉ lấy ID để tránh N+1 problem)
    @Query("SELECT m.movieId FROM Movie m WHERE m.fromDate < :currentDate")
    Page<Long> findNowShowingMovieIds(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    // Danh sách ID phim sắp chiếu (chỉ lấy ID để tránh N+1 problem)
    @Query("SELECT m.movieId FROM Movie m WHERE m.fromDate > :currentDate")
    Page<Long> findComingSoonMovieIds(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    // Lấy phim với tất cả quan hệ được tối ưu
    @Query("SELECT m FROM Movie m " +
            "LEFT JOIN FETCH m.movieTypes mt " +
            "LEFT JOIN FETCH mt.type " +
            "LEFT JOIN FETCH m.versions " +
            "LEFT JOIN FETCH m.ageRating " +
            "WHERE m.movieId = :id")
    Optional<Movie> findMovieWithAllRelationsById(@Param("id") Long id);

    // Truy vấn native để lấy chi tiết phim (đã có)
    @Query(value = """
           SELECT DISTINCT
                                                      m.movie_id,
                                                      m.movie_name,
                                                      m.movie_name_vn,
                                                      m.movie_name_en,
                                                      m.director,
                                                      m.actor,
                                                      m.content,
                                                      m.duration,
                                                      m.production_company,
                                                      m.rating_code,
                                                      m.from_date,
                                                      m.to_date,
                                                      m.large_image_url,
                                                      m.small_image_url,
                                                      m.trailer_url,
                                                      v.version_id,
                                                      v.version_name,
                                                      v.description AS version_description,
                                                      st.showtime_id,
                                                      st.available_seats,
                                                      cr.room_name AS room_number,
                                                      sd.show_date_id,
                                                      sd.show_date,
                                                      s.schedule_id,
                                                      s.schedule_time,
                                                    ag.description,
                                                    st.available_seats,
                                                    cr.seat_quantity
                                                  FROM
                                                      public.movie m
                                                      LEFT JOIN public.movie_version mv ON m.movie_id = mv.movie_id
                                                      LEFT JOIN public.version v ON mv.version_id = v.version_id
                                                      LEFT JOIN public.showtime st ON m.movie_id = st.movie_id
                                                          AND st.version_id = mv.version_id
                                                      LEFT JOIN public.movie_age_rating ag ON ag.rating_code = m.rating_code
                                                      LEFT JOIN public.show_dates sd ON st.show_date_id = sd.show_date_id
                                                      LEFT JOIN public.schedule s ON st.schedule_id = s.schedule_id
                                                      LEFT JOIN public.cinema_room cr ON st.room_id = cr.room_id
                                                  WHERE
                                                      m.movie_id = :movieId
                                                    AND (sd.show_date IS NULL OR sd.show_date >= CURRENT_DATE)
                                                  ORDER BY
                                                              v.version_id,
                                                      sd.show_date NULLS LAST,
                                                      s.schedule_time NULLS LAST;
        """, nativeQuery = true)
    List<Object[]> findMovieDetailsById(@Param("movieId") Long movieId);



    @Query("SELECT m FROM Movie m JOIN FETCH m.versions")
    List<Movie> findAllWithVersions();


    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.movieTypes mt LEFT JOIN FETCH mt.type " +
            "LEFT JOIN FETCH m.ageRating " +
            "WHERE m.fromDate > :currentDate")
    List<Movie> findAllComingSoonMovies(@Param("currentDate") LocalDate currentDate);

    /**
     * Find movies by a list of genre IDs (types)
     */
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.types t " +
            "WHERE m.fromDate >= :currentDate AND m.toDate < :currentDate AND t.typeId IN :genreIds AND m.movieId <> :excludedMovieId")
    List<Movie> findRelatedMoviesByGenres(@Param("genreIds") List<Long> genreIds,
                                          @Param("excludedMovieId") Long excludedMovieId, @Param("currentDate") LocalDate currentDate);

    /**
     * Get featured (hot) movies ordered by fromDate descending (latest first)
     */
    @Query("SELECT m FROM Movie m WHERE m.featured IS NOT NULL ORDER BY m.featured ASC")
    List<Movie> findFeaturedMovies();


    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.movieTypes mt LEFT JOIN FETCH mt.type " +
            "WHERE LOWER(m.movieNameVn) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.movieNameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Movie> findByMovieNameVnContainingIgnoreCaseOrMovieNameEnContainingIgnoreCase(
            @Param("keyword") String keyword, PageRequest pageRequest);
    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.movieTypes LEFT JOIN FETCH m.versions LEFT JOIN FETCH m.ageRating")
    Page<Movie> findAllWithTypesAndVersions(Pageable pageable);

    boolean existsByMovieNameIgnoreCase(String movieName);
    boolean existsByMovieNameEnIgnoreCase(String movieNameEn);
    boolean existsByMovieNameVnIgnoreCase(String movieNameVn);
    boolean existsByMovieNameAndMovieIdNot(String movieName, Long movieId);

    boolean existsByMovieNameVnAndMovieIdNot(String movieNameVn, Long movieId);

    boolean existsByMovieNameEnAndMovieIdNot(String movieNameEn, Long movieId);
}
