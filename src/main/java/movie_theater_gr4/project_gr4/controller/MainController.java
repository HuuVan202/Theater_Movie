package movie_theater_gr4.project_gr4.controller;

import movie_theater_gr4.project_gr4.dto.AccountDTO;
import movie_theater_gr4.project_gr4.dto.MovieDTO;
import movie_theater_gr4.project_gr4.dto.PromotionDTO;
import movie_theater_gr4.project_gr4.service.MovieService;
import movie_theater_gr4.project_gr4.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    private final MovieService movieService;
    private final PromotionService promotionService;

    @Autowired
    public MainController(MovieService movieService, PromotionService promotionService) {
        this.movieService = movieService;
        this.promotionService = promotionService;
    }

    @GetMapping({"/","/home"})
    public String home(Model model) {
//        List<MovieDTO> nowShowingMovies = movieService.getAllNowShowingMovies();
//        model.addAttribute("nowShowingMovies", nowShowingMovies);
//        model.addAttribute("nowShowingEmpty", nowShowingMovies == null || nowShowingMovies.isEmpty());
        List<MovieDTO> featuredMovies = movieService.getFeaturedMovies();
        model.addAttribute("featuredMovies", featuredMovies);
        model.addAttribute("featuredMoviesEmpty", featuredMovies == null || featuredMovies.isEmpty());
        model.addAttribute("latestPromotions", promotionService.getLatestPromotions());
        return "home";
    }

    @GetMapping("/auth")
    public String showAuthPage(Model model) {
        return "auth";
    }

    @GetMapping("/authAdmin")
    public String showAdminAuthPage(Model model) {
        return "loginAdmin";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/support")
    public String support() {
        return "support";
    }

    @GetMapping("/refund-policy")
    public String refundPolicy() {
        return "refund-policy";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy";
    }

    @GetMapping("/news")
    public String news(Model model) {
        List<PromotionDTO> promotions = promotionService.getAllActivePromotions();
        model.addAttribute("promotions", promotions != null ? promotions : new ArrayList<>());
        model.addAttribute("promotionsEmpty", promotions == null || promotions.isEmpty());
        return "news";
    }

    @GetMapping("/booking-guide")
    public String bookingGuide() {
        return "booking-guide";
    }

}
