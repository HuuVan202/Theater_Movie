package movie_theater_gr4.project_gr4.controller;

import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.dto.MovieDTO;
import movie_theater_gr4.project_gr4.dto.MovieSearchDTO;
import movie_theater_gr4.project_gr4.model.Type;
import movie_theater_gr4.project_gr4.repository.TypeRepository;
import movie_theater_gr4.project_gr4.service.MovieDocumentService;
import movie_theater_gr4.project_gr4.service.MovieService;
import movie_theater_gr4.project_gr4.service.TypeService;
import movie_theater_gr4.project_gr4.util.GenreTranslationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MovieListingController {

    private final MovieService movieService;
    private final TypeRepository typeRepository;
    private final GenreTranslationUtil genreTranslationUtil;
    private List<Type> cachedGenres; // Cache for all genres
    private final MovieDocumentService movieDocumentService;
    private final TypeService typeService ;



    // Helper class to hold both original genre name and its English key
    private class GenreDisplayData {
        private final String originalName;  // Original database name (Vietnamese)
        private final String translationKey; // English key for messages.properties

        public GenreDisplayData(String originalName, String translationKey) {
            this.originalName = originalName;
            this.translationKey = translationKey;
        }

        public String getOriginalName() {
            return originalName;
        }

        public String getTranslationKey() {
            return translationKey;
        }
    }

    // Convert a list of Type objects to GenreDisplayData objects
    private List<GenreDisplayData> prepareGenresForDisplay(List<Type> genres) {
        return genres.stream()
                .map(type -> new GenreDisplayData(
                        type.getTypeName(),
                        genreTranslationUtil.toEnglishKey(type.getTypeName())
                ))
                .collect(Collectors.toList());
    }

    @GetMapping({"/nowShowing"})
    public String nowShowing(Model model,
                             @RequestParam(defaultValue = "0") int nowShowingPage,
                             @RequestParam(required = false) String searchQuery,
                             @RequestParam(required = false) List<String> genres) {

        if (nowShowingPage < 0) {
            nowShowingPage = 0;
        }

        // Get only genres of currently showing movies and convert them to display format
        List<Type> nowShowingGenres = movieService.getNowShowingMovieGenres();
        model.addAttribute("allGenres", prepareGenresForDisplay(nowShowingGenres));

        Sort sort = Sort.by("movieName").ascending();
        Pageable nowShowingPageable = PageRequest.of(nowShowingPage, 12, sort);
        Page<MovieSearchDTO> nowShowingMovies;

        try {

            Page<MovieSearchDTO> moviePage;
            PageRequest pageRequest = PageRequest.of(nowShowingPage, 10, Sort.by(Sort.Direction.ASC, "movieId"));

            List<String> normalizedGenres = genres != null ? genres.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()) : null;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                nowShowingMovies = movieDocumentService.searchNowShowingMovieDocument(searchQuery, genres, pageRequest);
            } else {
                // Use cached result if available
                nowShowingMovies = movieService.getNowShowingSearchMovies(nowShowingPageable);
            }




            model.addAttribute("nowShowingMovies", nowShowingMovies.getContent());
            model.addAttribute("nowShowingCurrentPage", nowShowingPage);
            model.addAttribute("nowShowingTotalPages", nowShowingMovies.getTotalPages());
            model.addAttribute("nowShowingEmpty", nowShowingMovies.isEmpty());
        } catch (Exception e) {
            model.addAttribute("nowShowingMovies", List.of());
            model.addAttribute("nowShowingCurrentPage", 0);
            model.addAttribute("nowShowingTotalPages", 0);
            model.addAttribute("nowShowingEmpty", true);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách phim. Vui lòng thử lại sau.");
        }
        List<Type> types = typeService.getAllTypes();
//        model.addAttribute("allGenres", types);
        model.addAttribute("searchQuery", searchQuery);
//        model.addAttribute("genre", genres);
        return "showingList";
    }

    @GetMapping("/comingSoon")
    public String comingSoon(Model model,
                             @RequestParam(defaultValue = "0") int comingSoonPage,
                             @RequestParam(required = false) String searchQuery,
                             @RequestParam(required = false) List<String> genres) {

        if (comingSoonPage < 0) {
            comingSoonPage = 0;
        }

        // Get only genres of coming soon movies
        List<Type> comingSoonGenres = movieService.getComingSoonMovieGenres();
        model.addAttribute("allGenres", prepareGenresForDisplay(comingSoonGenres));

        Sort sort = Sort.by("movieName").ascending();
        Pageable comingSoonPageable = PageRequest.of(comingSoonPage, 12, sort);
        Page<MovieSearchDTO> comingSoonMovies;

        try {
            Page<MovieSearchDTO> moviePage;
            PageRequest pageRequest = PageRequest.of(comingSoonPage, 10, Sort.by(Sort.Direction.ASC, "movieId"));

            List<String> normalizedGenres = genres != null ? genres.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()) : null;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                comingSoonMovies = movieDocumentService.searchComingSoonMovieDocument(searchQuery, genres, pageRequest );
            }  else {
                // Use cached result if available
                comingSoonMovies = movieService.getComingSoonSearchMoviesCached(comingSoonPageable);
            }
//            List<Type> types = typeService.getAllTypes();
//            model.addAttribute("allGenres", types);
            model.addAttribute("comingSoonMovies", comingSoonMovies.getContent());
            model.addAttribute("comingSoonCurrentPage", comingSoonPage);
            model.addAttribute("comingSoonTotalPages", comingSoonMovies.getTotalPages());
            model.addAttribute("comingSoonEmpty", comingSoonMovies.isEmpty());
        } catch (Exception e) {
            model.addAttribute("comingSoonMovies", List.of());
            model.addAttribute("comingSoonCurrentPage", 0);
            model.addAttribute("comingSoonTotalPages", 0);
            model.addAttribute("comingSoonEmpty", true);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách phim. Vui lòng thử lại sau.");
        }

        model.addAttribute("searchQuery", searchQuery);
        List<Type> types = typeService.getAllTypes();
//        model.addAttribute("allGenres", types);
//        model.addAttribute("genre", genres);
        return "commingList";
    }
}
