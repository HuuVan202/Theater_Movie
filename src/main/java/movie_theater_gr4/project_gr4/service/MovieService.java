package movie_theater_gr4.project_gr4.service;


import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.dto.*;
import movie_theater_gr4.project_gr4.model.*;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;
import movie_theater_gr4.project_gr4.repository.*;
import movie_theater_gr4.project_gr4.util.GenreTranslationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {
    @Autowired
    private  MovieRepository movieRepository;
    @Autowired
    private  VersionRepository versionRepository;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private ShowTimeRepository  showtimeRepository;
    @Autowired
    private MovieAgeRatingRepository movieAgeRatingRepository;
    @Autowired
    private GenreTranslationUtil genreTranslationUtil;
    @Autowired
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    //    @Autowired
    private MovieDocumentService movieDocumentService;

    //    private final MovieElasticSearchRepository movieElasticSearchRepository;
    @Transactional(readOnly = true)
    @Cacheable(value = "movieDetail", key = "#id")
    public MovieDTO getMovieDetail(Long id) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return null;
        }

        Movie movie = movieOpt.get();

        List<VersionDTO> versionDTOs = movie.getVersions() != null
                ? movie.getVersions().stream()
                .map(v -> VersionDTO.builder()
                        .versionId(v.getVersionId())
                        .versionName(v.getVersionName())
                        .description(v.getDescription())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<String> genreNames = movie.getTypes() != null
                ? movie.getTypes().stream()
                .map(Type::getTypeName)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return MovieDTO.builder()

                .movieId(movie.getMovieId())
                .movieName(movie.getMovieName())
                .movieNameVn(movie.getMovieNameVn())
                .movieNameEn(movie.getMovieNameEn())
                .director(movie.getDirector())
                .actor(movie.getActor())
                .content(movie.getContent())
                .duration(movie.getDuration())
                .fromDate(movie.getFromDate())
                .toDate(movie.getToDate())
                .smallImageUrl(movie.getSmallImageUrl())
                .trailerUrl(movie.getTrailerUrl())
                .largeImageUrl(movie.getLargeImageUrl())
                .productionCompany(movie.getProductionCompany())
                .ageRating(movie.getAgeRating())
                .versions(versionDTOs)
                .genres(genreNames)
                .build();

    }


    @Transactional
    public MovieDTOCRUD getMovieDetailCRUD(Long id) {
        return movieRepository.findMovieWithTypesById(id)
                .map(this::convertToDTOCRUD)
                .orElse(null);
    }



    @Transactional(readOnly = true)
    public Page<MovieDTO> getAllMovies(Pageable pageable) {
        Page<Movie> movies = movieRepository.findAll(pageable);
        List<MovieDTO> movieDTOs = movies.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movieDTOs, pageable, movies.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getAllMoviesWithSort(PageRequest pageRequest) {
        Page<Movie> movies = movieRepository.findAll(pageRequest);
        return movies.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovieSearchDTO> getAllMoviesWithSortA(PageRequest pageRequest) {
        Page<Movie> movies = movieRepository.findAll(pageRequest);
        List<MovieSearchDTO> dtoList = movies.getContent().stream()
                .map(this::convertToMovieSearchDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageRequest, movies.getTotalElements());
    }

    public boolean existsByMovieName(String movieName) {
        return movieRepository.existsByMovieNameIgnoreCase(movieName);
    }
    public boolean existsByMovieNameVn(String movieNameVn) {
        return movieRepository.existsByMovieNameVnIgnoreCase(movieNameVn);
    }
    public boolean existsByMovieNameEn(String movieNameEn) {
        return movieRepository.existsByMovieNameEnIgnoreCase(movieNameEn);
    }

    public MovieSearchDTO convertToMovieSearchDTO(Movie movie) {
        MovieSearchDTO dto = new MovieSearchDTO();
        List<String> genres = typeRepository.findGenresByMovieId(movie.getMovieId());
        dto.setMovieId(movie.getMovieId());
        dto.setMovieName(movie.getMovieName());
        dto.setMovieNameVn(movie.getMovieNameVn());
        dto.setMovieNameEn(movie.getMovieNameEn());
        dto.setContent(movie.getContent());
        dto.setDuration(movie.getDuration());
        dto.setDirector(movie.getDirector());
        dto.setActor(movie.getActor());
        dto.setProductionCompany(movie.getProductionCompany());
        dto.setFromDate(movie.getFromDate());
        dto.setToDate(movie.getToDate());
        dto.setLargeImageUrl(movie.getLargeImageUrl());
        dto.setSmallImageUrl(movie.getSmallImageUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setFeatured(movie.getFeatured());
        dto.setGenres(genres);
        dto.setAgeRating(movie.getAgeRating() != null ? movie.getAgeRating() : null);
        return dto;
    }


    @Transactional(readOnly = true)
    public Page<MovieDTO> searchMoviesByName(String keyword, PageRequest pageRequest) {
        return movieRepository.findByMovieNameVnContainingIgnoreCaseOrMovieNameEnContainingIgnoreCase(
                keyword, pageRequest).map(this::convertToDTO);
    }


    @Transactional(readOnly = true)
    @Cacheable(value = "nowShowingMovies")
    public List<MovieDTO> getAllNowShowingMovies() {
        LocalDate currentDate = LocalDate.now();
        List<Movie> movies = movieRepository.findAllNowShowingMovies(currentDate);
        return movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getNowShowingMovies(Pageable pageable) {
        LocalDate currentDate = LocalDate.now();
        Page<Movie> movies = movieRepository.findNowShowingMovies(currentDate, pageable);
        List<MovieDTO> movieDTOs = movies.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movieDTOs, pageable, movies.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<MovieSearchDTO> getNowShowingSearchMovies(Pageable pageable) {
        LocalDate currentDate = LocalDate.now();
        Page<Movie> movies = movieRepository.findNowShowingMovies(currentDate, pageable);
        List<MovieSearchDTO> movieDTOs = movies.getContent().stream()
                .map(this::convertToMovieSearchDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movieDTOs, pageable, movies.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getComingSoonMovies(Pageable pageable, String searchQuery, String genre) {
        LocalDate currentDate = LocalDate.now();
        Page<Movie> movies;
        movies = movieRepository.findComingSoonMovies(currentDate, pageable);
        List<MovieDTO> movieDTOs = movies.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movieDTOs, pageable, movies.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<MovieSearchDTO> getComingSoonSearchMovies(Pageable pageable, String searchQuery, String genre) {
        LocalDate currentDate = LocalDate.now();
        Page<Movie> movies;
        movies = movieRepository.findComingSoonMovies(currentDate, pageable);
        List<MovieSearchDTO> movieDTOs = movies.getContent().stream()
                .map(this::convertToMovieSearchDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(movieDTOs, pageable, movies.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> searchComingSoonMovies(String searchQuery, Pageable pageable) {
        LocalDate now = LocalDate.now();
        return movieRepository.findByMovieNameContainingIgnoreCaseAndFromDateAfter(
                searchQuery,
                now,
                pageable
        ).map(this::convertToDTO);
    }

    @Transactional
    public void createMovie(MovieDTOCRUD movieDTOCRUD) {
        Movie movie = MovieDTOCRUDConvertToEntity(movieDTOCRUD);


        movie = movieRepository.save(movie);
//        MovieDocument movieDocument = MovieDocument.fromEntity(movieRepository.findById(movieDTOCRUD.getMovieId()).get());
//        movieElasticSearchRepository.save(movieDocument);
        if (movieDTOCRUD.getGenres() != null && !movieDTOCRUD.getGenres().isEmpty()) {
            for (Long typeId : movieDTOCRUD.getGenres()) {
                Type type = typeRepository.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("Type not found with id: " + typeId));

            }
        }

    }



    @Transactional
    public Movie updateMovie(Long movieId, MovieDTOCRUD movieDTOCRUD) {
        // Tìm kiếm phim theo ID
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        // Cập nhật các trường cơ bản
        existingMovie.setMovieName(movieDTOCRUD.getMovieName());
        existingMovie.setMovieNameVn(movieDTOCRUD.getMovieNameVn());
        existingMovie.setMovieNameEn(movieDTOCRUD.getMovieNameEn());
        existingMovie.setDirector(movieDTOCRUD.getDirector());
        existingMovie.setActor(movieDTOCRUD.getActor());
        existingMovie.setProductionCompany(movieDTOCRUD.getProductionCompany());
        existingMovie.setContent(movieDTOCRUD.getContent());
        existingMovie.setDuration(movieDTOCRUD.getDuration());
        existingMovie.setFromDate(movieDTOCRUD.getFromDate());
        existingMovie.setToDate(movieDTOCRUD.getToDate());
        existingMovie.setSmallImageUrl(movieDTOCRUD.getSmallImageUrl());
        existingMovie.setLargeImageUrl(movieDTOCRUD.getLargeImageUrl());
        existingMovie.setTrailerUrl(movieDTOCRUD.getTrailerUrl());
        existingMovie.setFeatured(movieDTOCRUD.getFeatured());
        MovieAgeRating ageRating = movieAgeRatingRepository.findAgeRatingByRatingCode(movieDTOCRUD.getAgeRating());
        existingMovie.setAgeRating(ageRating);

        // Cập nhật versions
        if (movieDTOCRUD.getVersions() != null && !movieDTOCRUD.getVersions().isEmpty()) {
            for (Long versionId : movieDTOCRUD.getVersions()) {
                Version version = versionRepository.findById(versionId)
                        .orElseThrow(() -> new RuntimeException("Version not found with id: " + versionId));
            }
            existingMovie.setVersions(versionRepository.findAllById(movieDTOCRUD.getVersions()));
        } else {
            existingMovie.setVersions(new ArrayList<>());
        }

        // Cập nhật genres
        if (movieDTOCRUD.getGenres() != null && !movieDTOCRUD.getGenres().isEmpty()) {
            for (Long typeId : movieDTOCRUD.getGenres()) {
                Type type = typeRepository.findById(typeId)
                        .orElseThrow(() -> new RuntimeException("Type not found with id: " + typeId));
            }
            existingMovie.setTypes(typeRepository.findAllById(movieDTOCRUD.getGenres()));
        } else {
            existingMovie.setTypes(new ArrayList<>());
        }

        // Lưu lại entity đã cập nhật
//        MovieDocument movieDocument = MovieDocument.fromEntity(movieRepository.findById(movieId).get());
//        movieElasticSearchRepository.save(movieDocument);
        movieRepository.save(existingMovie);
        return existingMovie;
    }

    @Transactional
    @CacheEvict(value = {"movieDetail", "movieDetailFull", "movieDetailComplete", "nowShowingMovies"}, allEntries = true)
    public void deleteMovie(Long id) {
        LocalDate currentDate =  LocalDate.now();
        if (showtimeRepository.existsByMovieIdAndShowDateGreaterThanOrEqual(id, currentDate)) {
            throw new RuntimeException("Cannot delete movie with ID " + id + " because it has upcoming showtimes.");
        }
        movieRepository.deleteById(id);
    }

//    @CacheEvict(value = {"movieDetail", "movieDetailFull", "movieDetailComplete", "nowShowingMovies"}, allEntries = true)
//    public void clearMovieCaches() {
//        // This method will clear all movie-related caches
//        // Can be called manually if needed
//    }


    private MovieDTO convertToDTO(Movie movie) {
        MovieAgeRating mar = movieAgeRatingRepository.findAgeRatingByMovieId(movie.getMovieId());
        return MovieDTO.builder()
                .movieId(movie.getMovieId())
                .movieName(movie.getMovieName())
                .movieNameVn(movie.getMovieNameVn())
                .movieNameEn(movie.getMovieNameEn())
                .productionCompany(movie.getProductionCompany())
                .director(movie.getDirector())
                .actor(movie.getActor())
                .content(movie.getContent())
                .duration(movie.getDuration())
                .versions(movie.getVersions().stream()
                        .map(version -> VersionDTO.builder()
                                .versionId(version.getVersionId())
                                .versionName(version.getVersionName())
                                .description(version.getDescription())
                                .build())
                        .collect(Collectors.toList()))

                .showDates(movie.getShowtimes().stream()
                        .map(showtime -> showtime.getShowDate().getShowDate())
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList()))

                .fromDate(movie.getFromDate())
                .toDate(movie.getToDate())
                .smallImageUrl(movie.getSmallImageUrl())
                .trailerUrl(movie.getTrailerUrl())
                .largeImageUrl(movie.getLargeImageUrl())
                .genres(movie.getMovieTypes().stream()
                        .map(movieType -> movieType.getType().getTypeName())
                        .collect(Collectors.toList()))
                .ageRating(movie.getAgeRating())

                .build();
    }





    private MovieDTOCRUD convertToDTOCRUD(Movie movie) {
        MovieDTOCRUD movieDTO = new MovieDTOCRUD();


        movieDTO.setMovieId(movie.getMovieId());
        movieDTO.setMovieName(movie.getMovieName());
        movieDTO.setMovieNameVn(movie.getMovieNameVn());
        movieDTO.setMovieNameEn(movie.getMovieNameEn());
        movieDTO.setProductionCompany(movie.getProductionCompany());
        movieDTO.setDirector(movie.getDirector());
        movieDTO.setActor(movie.getActor());
        movieDTO.setContent(movie.getContent());
        movieDTO.setDuration(movie.getDuration());
        movieDTO.setFromDate(movie.getFromDate());
        movieDTO.setToDate(movie.getToDate());
        movieDTO.setSmallImageUrl(movie.getSmallImageUrl());
        movieDTO.setLargeImageUrl(movie.getLargeImageUrl());
        movieDTO.setTrailerUrl(movie.getTrailerUrl());
        movieDTO.setFeatured(movie.getFeatured());
        // Ánh xạ MovieAgeRating sang mã rating code
        if (movie.getAgeRating() != null) {
            movieDTO.setAgeRating(movie.getAgeRating().getRatingCode());
        }

        // Ánh xạ danh sách Versions sang danh sách ID
        if (movie.getVersions() != null) {
            List<Long> versionIds = movie.getVersions().stream()
                    .map(Version::getVersionId)
                    .collect(Collectors.toList());
            movieDTO.setVersions(versionIds);
        }

        // Ánh xạ danh sách Types (Genres) sang danh sách ID
        if (movie.getTypes() != null) {
            List<Long> genreIds = movie.getTypes().stream()
                    .map(Type::getTypeId)
                    .collect(Collectors.toList());
            movieDTO.setGenres(genreIds);
        }

        return movieDTO;
    }




    public Movie MovieDTOCRUDConvertToEntity(MovieDTOCRUD movieDTO) {
        Movie movie = new Movie();

        List<Version> allVersions = versionRepository.findAll();
        List<Type> allTypes = typeRepository.findAll();
        List<MovieAgeRating> allRatings = movieAgeRatingRepository.findAll();

        movie.setMovieName(movieDTO.getMovieName());
        movie.setMovieNameVn(movieDTO.getMovieNameVn());
        movie.setMovieNameEn(movieDTO.getMovieNameEn());
        movie.setDirector(movieDTO.getDirector());
        movie.setActor(movieDTO.getActor());
        movie.setProductionCompany(movieDTO.getProductionCompany());

        movie.setContent(movieDTO.getContent());
        movie.setDuration(movieDTO.getDuration());
        movie.setFromDate(movieDTO.getFromDate());
        movie.setToDate(movieDTO.getToDate());
        movie.setSmallImageUrl(movieDTO.getSmallImageUrl());
        movie.setLargeImageUrl(movieDTO.getLargeImageUrl());
        movie.setTrailerUrl(movieDTO.getTrailerUrl());

        MovieAgeRating ageRating = movieAgeRatingRepository.findAgeRatingByRatingCode(movieDTO.getAgeRating());

        movie.setAgeRating(ageRating);

        movie.setFeatured(movieDTO.getFeatured());

        movie.setVersions(versionRepository.findAllById(movieDTO.getVersions()));
        movie.setTypes(typeRepository.findAllById(movieDTO.getGenres()));
        movieRepository.save(movie);

        movie.setMovieTypes(new ArrayList<>());
        movie.setShowtimes(new ArrayList<>());
        return movie;
    }



    private LocalDate parseDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        }
        if (dateObj instanceof String) {
            try {
                return LocalDate.parse((String) dateObj, DATE_FORMATTER);
            } catch (Exception e) {
                // Log error or handle invalid date format
                return null;
            }
        }
        return null;
    }


    @Transactional(readOnly = true)
    public Page<MovieDTO> searchNowShowingMovies(String searchQuery, Pageable pageable) {
        LocalDate now = LocalDate.now();
        return movieRepository.findByMovieNameContainingIgnoreCaseAndFromDateBeforeAndToDateAfterOrToDateIsNull(
                searchQuery,
                now,
                now,
                pageable
        ).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getNowShowingMoviesByGenre(String genre, Pageable pageable) {
        LocalDate now = LocalDate.now();
        return movieRepository.findNowShowingMoviesByGenre(now, genre, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MovieDTO> getComingSoonMoviesByGenre(String genre, Pageable pageable) {
        LocalDate now = LocalDate.now();
        return movieRepository.findComingSoonMoviesByGenre(now, genre, pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "nowShowingMoviesPage", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MovieDTO> getNowShowingMoviesCached(Pageable pageable) {
        return getNowShowingMovies(pageable);
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "nowShowingMoviesPage", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MovieSearchDTO> getNowShowingSearchMoviesCached(Pageable pageable) {
        return getNowShowingSearchMovies(pageable);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comingSoonMoviesPage", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MovieDTO> getComingSoonMoviesCached(Pageable pageable) {
        return getComingSoonMovies(pageable, null, null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comingSoonMoviesPage", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<MovieSearchDTO> getComingSoonSearchMoviesCached(Pageable pageable) {
        return getComingSoonSearchMovies(pageable, null, null);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "movieDetails", key = "#movieId")
    public MovieDetailDTO getMovieDetails(Long movieId) {
        List<Object[]> results = movieRepository.findMovieDetailsById(movieId);
        if (results.isEmpty()) {
            return null;
        }

        // Lấy danh sách thể loại
        List<String> genres = typeRepository.findGenresByMovieId(movieId);

        // Khởi tạo các map để nhóm dữ liệu
        Map<Long, MovieDetailDTO> movieMap = new HashMap<>();
        Map<Long, VersionDetailDTO> versionMap = new HashMap<>();
        Map<String, ShowDateDTO> showDateMap = new HashMap<>(); // Sử dụng key kết hợp versionId và showDateId

        for (Object[] row : results) {
            Long movieIdFromRow = ((Number) row[0]).longValue();

            // Tạo MovieDetailDTO nếu chưa có
            MovieDetailDTO movieDTO = movieMap.computeIfAbsent(movieIdFromRow, k -> MovieDetailDTO.builder()
                    .movieId(movieIdFromRow)
                    .movieName((String) row[1])
                    .movieNameVn((String) row[2])
                    .movieNameEn((String) row[3])
                    .director((String) row[4])
                    .actor((String) row[5])
                    .content((String) row[6])
                    .duration(row[7] != null ? ((Number) row[7]).intValue() : null)
                    .productionCompany((String) row[8])
                    .ratingCode((String) row[9])
                    .fromDate(parseDate(row[10]))
                    .toDate(parseDate(row[11]))
                    .largeImageUrl((String) row[12])
                    .smallImageUrl((String) row[13])
                    .trailerUrl((String) row[14])
                    .versions(new ArrayList<>())
                    .genres(genres)
                    .ageRating((String) row[25])
                    .build());

            Long versionId = row[15] != null ? ((Number) row[15]).longValue() : null;
            if (versionId != null) {
                // Tạo VersionDetailDTO nếu chưa có
                VersionDetailDTO versionDTO = versionMap.computeIfAbsent(versionId, k -> VersionDetailDTO.builder()
                        .versionId(versionId)
                        .versionName((String) row[16])
                        .description((String) row[17])
                        .showDates(new ArrayList<>())
                        .build());

                Long showDateId = row[21] != null ? ((Number) row[21]).longValue() : null;
                if (showDateId != null) {
                    // Sử dụng key kết hợp versionId và showDateId để đảm bảo showDateDTO không bị lặp
                    String showDateKey = versionId + "_" + showDateId;
                    ShowDateDTO showDateDTO = showDateMap.computeIfAbsent(showDateKey, k -> ShowDateDTO.builder()
                            .showDateId(showDateId)
                            .showDate(parseDate(row[22]))
                            .schedules(new ArrayList<>())
                            .build());

                    // Đảm bảo showDateDTO chỉ được thêm một lần vào đúng version
                    if (!versionDTO.getShowDates().contains(showDateDTO)) {
                        versionDTO.getShowDates().add(showDateDTO);
                    }

                    Long showtimeId = row[18] != null ? ((Number) row[18]).longValue() : null;
                    if (showtimeId != null) {
                        // Tạo ScheduleDTO
                        ScheduleDTO scheduleDTO = ScheduleDTO.builder()
                                .showtimeId(showtimeId)
                                .scheduleId(((Number) row[23]).longValue())
                                .scheduleTime(row[24] != null ? LocalTime.parse(((java.sql.Time) row[24]).toString()) : null)
                                .availableSeats(((Number) row[19]).intValue())
                                .roomNumber((String) row[20])
                                .seatQuantity(row[26] != null ? ((Number) row[26]).intValue() : null) // Map seat_quantity
                                .build();

                        // Đảm bảo scheduleDTO chỉ được thêm một lần
                        if (!showDateDTO.getSchedules().contains(scheduleDTO)) {
                            showDateDTO.getSchedules().add(scheduleDTO);
                        }
                    }
                }

                // Đảm bảo versionDTO chỉ được thêm một lần vào movieDTO
                if (!movieDTO.getVersions().contains(versionDTO)) {
                    movieDTO.getVersions().add(versionDTO);
                }
            }
        }

        MovieDetailDTO movieDTO = movieMap.get(movieId);
        if (movieDTO != null) {
            // Sắp xếp showDates và schedules
            movieDTO.getVersions().forEach(version -> {
                version.getShowDates().sort(Comparator.comparing(ShowDateDTO::getShowDate, Comparator.nullsLast(Comparator.naturalOrder())));
                version.getShowDates().forEach(showDate ->
                        showDate.getSchedules().sort(Comparator.comparing(ScheduleDTO::getScheduleTime, Comparator.nullsLast(Comparator.naturalOrder()))));
            });

            // Loại bỏ các version không có showDates
            movieDTO.setVersions(movieDTO.getVersions().stream()
                    .filter(version -> !version.getShowDates().isEmpty())
                    .collect(Collectors.toList()));
        }

        return movieDTO;
    }
    @CacheEvict(value = {"movieDetail", "movieDetailFull", "movieDetailComplete", "nowShowingMovies"}, allEntries = true)
    public void clearMovieCaches() {
        // This method will clear all movie-related caches
        // Can be called manually if needed
    }

    @CacheEvict(value = {"movieDetail", "movieDetails"}, key = "#movieId")
    public void clearMovieCacheById(Long movieId) {
        // This method will clear cache for a specific movieId
    }


    public Page<MovieDTO> searchMovies(String keyword, PageRequest pageRequest) {
        return movieRepository.findByMovieNameVnContainingIgnoreCaseOrMovieNameEnContainingIgnoreCase(
                keyword,  pageRequest).map(this::convertToDTO);
    }

    /**
     * Get all unique genres of currently showing movies
     * @return List of Type objects representing genres in use by currently showing movies
     */
    @Transactional(readOnly = true)
    public List<Type> getNowShowingMovieGenres() {
        LocalDate currentDate = LocalDate.now();
        List<Movie> nowShowingMovies = movieRepository.findAllNowShowingMovies(currentDate);

        // Collect all unique genres from now showing movies
        return nowShowingMovies.stream()
                .flatMap(movie -> movie.getMovieTypes().stream())
                .map(MovieType::getType)
                .distinct()
                .sorted(Comparator.comparing(Type::getTypeName))
                .collect(Collectors.toList());
    }

    /**
     * Get all unique genres of coming soon movies
     * @return List of Type objects representing genres in use by coming soon movies
     */
    @Transactional(readOnly = true)
    public List<Type> getComingSoonMovieGenres() {
        LocalDate currentDate = LocalDate.now();
        // Find movies with fromDate in the future
        List<Movie> comingSoonMovies = movieRepository.findAllComingSoonMovies(currentDate);

        // Collect all unique genres from coming soon movies
        return comingSoonMovies.stream()
                .flatMap(movie -> movie.getMovieTypes().stream())
                .map(MovieType::getType)
                .distinct()
                .sorted(Comparator.comparing(Type::getTypeName))
                .collect(Collectors.toList());
    }

    /**
     * Find movies related by genres (excluding the current movie)
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> getRelatedMoviesByGenres(Long movieId) {
        LocalDate currentDate = LocalDate.now();
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if (movieOpt.isEmpty()) return List.of();
        Movie movie = movieOpt.get();
        List<Type> genres = movie.getTypes();
        if (genres == null || genres.isEmpty()) return List.of();
        List<Long> genreIds = genres.stream().map(Type::getTypeId).collect(Collectors.toList());
        List<Movie> relatedMovies = movieRepository.findRelatedMoviesByGenres(genreIds, movieId, currentDate);
        // Exclude the current movie
        return relatedMovies.stream()
                .filter(m -> !m.getMovieId().equals(movieId))
                .map(this::convertToDTO)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get featured (hot) movies for display
     */
    @Transactional(readOnly = true)
    public List<MovieDTO> getFeaturedMovies() {
        List<Movie> featured = movieRepository.findFeaturedMovies();
        return featured.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    // Kiểm tra movieName có trùng lặp, loại trừ phim hiện tại
    public boolean existsByMovieNameAndNotId(String movieName, Long excludeId) {
        return movieRepository.existsByMovieNameAndMovieIdNot(movieName.trim(), excludeId);
    }

    // Kiểm tra movieNameVn có trùng lặp, loại trừ phim hiện tại
    public boolean existsByMovieNameVnAndNotId(String movieNameVn, Long excludeId) {
        return movieRepository.existsByMovieNameVnAndMovieIdNot(movieNameVn.trim(), excludeId);
    }

    // Kiểm tra movieNameEn có trùng lặp, loại trừ phim hiện tại
    public boolean existsByMovieNameEnAndNotId(String movieNameEn, Long excludeId) {
        return movieRepository.existsByMovieNameEnAndMovieIdNot(movieNameEn.trim(), excludeId);
    }



}
