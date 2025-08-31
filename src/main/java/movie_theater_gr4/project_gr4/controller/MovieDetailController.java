package movie_theater_gr4.project_gr4.controller;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.dto.MovieDetailDTO;
import movie_theater_gr4.project_gr4.dto.MovieResponseDTO;
import movie_theater_gr4.project_gr4.model.Schedule;
import movie_theater_gr4.project_gr4.model.ShowDate;
import movie_theater_gr4.project_gr4.model.Showtime;
import movie_theater_gr4.project_gr4.model.Version;
import movie_theater_gr4.project_gr4.repository.ScheduleRepository;
import movie_theater_gr4.project_gr4.repository.ShowDateRepository;
import movie_theater_gr4.project_gr4.repository.ShowTimeRepository;
import movie_theater_gr4.project_gr4.repository.VersionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import movie_theater_gr4.project_gr4.dto.MovieDTO;
import movie_theater_gr4.project_gr4.service.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import movie_theater_gr4.project_gr4.util.GenreTranslationUtil;

@Controller
@RequiredArgsConstructor
public class MovieDetailController {


    private final MovieService movieService;
    private final ShowDateRepository showDateRepository;
    private final ShowTimeRepository showTimeRepository;
    private final ScheduleRepository scheduleRepository;
    private final GenreTranslationUtil genreTranslationUtil;
    private final VersionRepository versionRepository;
    @GetMapping("/detail/{id}")
    public String getMovieDetail(@PathVariable Long id, Model model) {
        try {
            // Lấy dữ liệu phim
            MovieDTO movieDTO = movieService.getMovieDetail(id);
            if (movieDTO == null) {
                return "error/404"; // trang not found
            }

            // Normalize genres for i18n
            if (movieDTO.getGenres() != null) {
                List<String> normalizedGenres = movieDTO.getGenres().stream()
                    .map(genreTranslationUtil::toEnglishKey)
                    .collect(Collectors.toList());
                movieDTO.setGenres(normalizedGenres);
            }

            // Lấy ngày chiếu và gán vào movieDTO
            List<ShowDate> showDates = showDateRepository.findDistinctShowDatesByMovieId(id);
            if (showDates != null && !showDates.isEmpty()) {
                showDates.sort(Comparator.comparing(ShowDate::getShowDate));
                List<LocalDate> localDates = showDates.stream()
                    .map(ShowDate::getShowDate)
                    .collect(Collectors.toList());
                movieDTO.setShowDates(localDates);
                System.out.println("DEBUG - showDates: " + localDates);
            } else {
                System.out.println("DEBUG - No show dates found, using default dates");
                // Tạo ngày mặc định nếu không có dữ liệu
                List<LocalDate> defaultDates = List.of(LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));
                movieDTO.setShowDates(defaultDates);
            }

            // Kiểm tra phiên bản phim
            if (movieDTO.getVersions() == null || movieDTO.getVersions().isEmpty()) {
                System.out.println("DEBUG - No versions found for movie ID: " + id);
                // Tạo phiên bản mặc định nếu không có dữ liệu
                movie_theater_gr4.project_gr4.dto.VersionDTO defaultVersion = new movie_theater_gr4.project_gr4.dto.VersionDTO();
                defaultVersion.setVersionId(1L);
                defaultVersion.setVersionName("2D - Phụ đề Việt");
                movieDTO.setVersions(List.of(defaultVersion));
            }
            System.out.println("DEBUG - versions: " + movieDTO.getVersions());

            // Lấy dữ liệu showtimes và schedules
            List<Showtime> showtimes = showTimeRepository.findDistinctShowtimesByMovie_MovieId(id);
            showtimes.sort(Comparator.comparing(Showtime::getId));
            List<Schedule> schedules = scheduleRepository.findDistinctScheduleByMovieId(id);
            schedules.sort(Comparator.comparing(Schedule::getScheduleTime));

            // Debug thông tin
//            System.out.println("DEBUG - schedules: " + schedules);
//            System.out.println("DEBUG - versions: " + movieDTO.getVersions());

            // Thêm dữ liệu vào model
//            model.addAttribute("showDates", showDates);
//            model.addAttribute("schedules_time", schedules);
//            model.addAttribute("showTimes", showtimes);
//            model.addAttribute("movie", movieDTO);

            MovieDetailDTO movie = movieService.getMovieDetails(id);
            if (movie == null) {
                model.addAttribute("error", "Phim với ID " + id + " không tồn tại");
                return "error";
            }

            List<Version> versions = versionRepository.findByMovieVersions_Movie_MovieId(id);
            model.addAttribute("movie", movie);
            model.addAttribute("versions", versions);


            // Thêm phim liên quan theo thể loại
            List<MovieDTO> relatedMovies = movieService.getRelatedMoviesByGenres(id);
            // Normalize genres for related movies
            if (relatedMovies != null) {
                for (MovieDTO related : relatedMovies) {
                    if (related.getGenres() != null) {
                        List<String> normalizedGenres = related.getGenres().stream()
                            .map(genreTranslationUtil::toEnglishKey)
                            .collect(Collectors.toList());
                        related.setGenres(normalizedGenres);
                    }
                }
            }
            model.addAttribute("relatedMovies", relatedMovies);
            // Thêm phim nổi bật
            List<MovieDTO> featuredMovies = movieService.getFeaturedMovies();
            model.addAttribute("featuredMovies", featuredMovies);

            return "movieDetails";
        } catch (Exception e) {
            System.err.println("Error loading movie detail: " + e.getMessage());
            e.printStackTrace();
            return "error/500"; // Internal server error
        }
    }
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<?> getMovieDetails(@PathVariable Long movieId) {
        try {
            MovieDetailDTO movieDetailDTO = movieService.getMovieDetails(movieId);
            if (movieDetailDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Phim với ID " + movieId + " không tồn tại");
            }
            return ResponseEntity.ok(movieDetailDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi lấy chi tiết phim: " + e.getMessage());
        }
    }
//
//    @GetMapping("/details/{movieId}")
//    public String getMovieDetails(@PathVariable Long movieId, Model model) {
//        if (movieId == null || movieId <= 0) {
//            model.addAttribute("error", "movieId phải là số dương");
//            return "error";
//        }
//
//        try {
//
//            MovieDetailDTO movie = movieService.getMovieDetails(movieId);
//            if (movie == null) {
//                model.addAttribute("error", "Phim với ID " + movieId + " không tồn tại");
//                return "error";
//            }
//            model.addAttribute("movie", movie);
//            return "movieTesting";
//        } catch (Exception e) {
//            model.addAttribute("error", "Đã xảy ra lỗi khi lấy chi tiết phim");
//            return "error";
//        }
//    }
    @GetMapping("/clear-cache")
    public ResponseEntity<String> clearMovieCache() {
        try {
            movieService.clearMovieCaches();
            return ResponseEntity.ok("Đã xóa cache thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa cache: " + e.getMessage());
        }
    }

}
