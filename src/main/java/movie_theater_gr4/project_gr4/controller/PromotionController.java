package movie_theater_gr4.project_gr4.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import movie_theater_gr4.project_gr4.dto.PromotionDTO;
import movie_theater_gr4.project_gr4.model.MoviePromotion;
import movie_theater_gr4.project_gr4.model.TicketType;
import movie_theater_gr4.project_gr4.repository.MoviePromotionRepository;
import movie_theater_gr4.project_gr4.repository.MovieRepository;
import movie_theater_gr4.project_gr4.repository.TicketTypeRepository;
import movie_theater_gr4.project_gr4.security.CustomUserDetails;
import movie_theater_gr4.project_gr4.service.NotificationService;
import movie_theater_gr4.project_gr4.service.PromotionService;
import movie_theater_gr4.project_gr4.service.TicketTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class PromotionController {
    private static final Logger logger = Logger.getLogger(PromotionController.class.getName());
    private final PromotionService promotionService;
    private final TicketTypeService ticketTypeService;
    private final MovieRepository movieRepository;
    private final MoviePromotionRepository moviePromotionRepository;
    private final Cloudinary cloudinary;
    private final NotificationService notificationService;

    public PromotionController(PromotionService promotionService, TicketTypeService ticketTypeService,
                               MovieRepository movieRepository, MoviePromotionRepository moviePromotionRepository,
                               Cloudinary cloudinary, NotificationService notificationService) {
        this.promotionService = promotionService;
        this.ticketTypeService = ticketTypeService;
        this.movieRepository = movieRepository;
        this.moviePromotionRepository = moviePromotionRepository;
        this.cloudinary = cloudinary;
        this.notificationService = notificationService;
    }

    @GetMapping("/admin/promotions/create-form")
    public String showCreatePromotionForm(Model model) {
        model.addAttribute("promotion", new PromotionDTO());
        model.addAttribute("ticketTypes", ticketTypeService.findAll());
        model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
        return "admin/promotion/create";
    }

    @PostMapping("/admin/promotions/create")
    public String createPromotion(@Valid @ModelAttribute("promotion") PromotionDTO promotionDTO,
                                  BindingResult bindingResult,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  Model model) {
        // Kiểm tra validate
        if (promotionDTO.getStartTime() != null && promotionDTO.getEndTime() != null &&
                promotionDTO.getEndTime().isBefore(promotionDTO.getStartTime())) {
            bindingResult.rejectValue("endTime", "error.endTime", "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (promotionDTO.getStartTime() != null && promotionDTO.getStartTime().isBefore(LocalDateTime.now())) {
            bindingResult.rejectValue("startTime", "error.startTime", "Thời gian bắt đầu không được trong quá khứ");
        }
        if (promotionDTO.getMinTickets() != null && promotionDTO.getMaxTickets() != null &&
                promotionDTO.getMaxTickets() < promotionDTO.getMinTickets()) {
            bindingResult.rejectValue("maxTickets", "error.maxTickets", "Số vé tối đa phải lớn hơn hoặc bằng số vé tối thiểu");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("ticketTypes", ticketTypeService.findAll());
            model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
            return "admin/promotion/create";
        }

        try {
            // Gọi service để lưu khuyến mãi và liên kết phim
            promotionService.addPromotion(promotionDTO, imageFile);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            int accountId = ((CustomUserDetails) authentication.getPrincipal()).getAccountId();
            notificationService.sendAndSaveNotification(accountId, promotionDTO.getTitle(), promotionDTO.getDetail());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("imageUrl", "error.imageUrl", e.getMessage());
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("ticketTypes", ticketTypeService.findAll());
            model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
            return "admin/promotion/create";
        } catch (Exception e) {
            bindingResult.rejectValue("imageUrl", "error.imageUrl", "Lỗi xử lý khuyến mãi: " + e.getMessage());
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("ticketTypes", ticketTypeService.findAll());
            model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
            return "admin/promotion/create";
        }
        return "redirect:/admin/promotions/list";
    }

    @PostMapping("/admin/promotions/update")
    public String updatePromotion(@Valid @ModelAttribute("promotion") PromotionDTO promotionDTO,
                                  BindingResult bindingResult,
                                  @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                  Model model) {
        // Kiểm tra validate
        if (promotionDTO.getStartTime() != null && promotionDTO.getEndTime() != null &&
                promotionDTO.getEndTime().isBefore(promotionDTO.getStartTime())) {
            bindingResult.rejectValue("endTime", "error.endTime", "Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if (promotionDTO.getMinTickets() != null && promotionDTO.getMaxTickets() != null &&
                promotionDTO.getMaxTickets() < promotionDTO.getMinTickets()) {
            bindingResult.rejectValue("maxTickets", "error.maxTickets", "Số vé tối đa phải lớn hơn hoặc bằng số vé tối thiểu");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("ticketTypes", ticketTypeService.findAll());
            model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
            model.addAttribute("currentTime", LocalDateTime.now());
            return "admin/promotion/edit";
        }

        try {
            // Gọi phương thức updatePromotion trong service
            promotionService.updatePromotion(promotionDTO, imageFile);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("imageUrl", "error.imageUrl", e.getMessage());
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("ticketTypes", ticketTypeService.findAll());
            model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
            model.addAttribute("currentTime", LocalDateTime.now());
            return "admin/promotion/edit";
        } catch (Exception e) {
            bindingResult.rejectValue("imageUrl", "error.imageUrl", "Lỗi xử lý khuyến mãi: " + e.getMessage());
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("ticketTypes", ticketTypeService.findAll());
            model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
            model.addAttribute("currentTime", LocalDateTime.now());
            return "admin/promotion/edit";
        }
        return "redirect:/admin/promotions/list";
    }

    @GetMapping("/admin/promotions/edit")
    public String editPromotionForm(@RequestParam Integer promotionId, Model model) {
        PromotionDTO promotion = promotionService.getPromotionById(promotionId);
        if (promotion == null) {
            return "redirect:/admin/promotions/list";
        }
        // Lấy movieId từ bảng movie_promotion
        MoviePromotion moviePromotion = moviePromotionRepository.findByPromotionId(promotionId);
        if (moviePromotion != null) {
            promotion.setMovieId(moviePromotion.getMovieId());
        }
        model.addAttribute("promotion", promotion);
        model.addAttribute("ticketTypes", ticketTypeService.findAll());
        model.addAttribute("movies", movieRepository.findAllNowShowingMovies(LocalDate.now().plusDays(200)));
        model.addAttribute("currentTime", LocalDateTime.now());
        return "admin/promotion/edit";
    }

    @PostMapping("/admin/promotions/delete")
    public String deletePromotion(@RequestParam Integer promotionId) {
        PromotionDTO promotion = promotionService.getPromotionById(promotionId);
        if (promotion == null) {
            return "redirect:/admin/promotions/list?error=promotion_not_found";
        }
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getStartTime() != null && promotion.getEndTime() != null &&
                !now.isBefore(promotion.getStartTime()) && !now.isAfter(promotion.getEndTime())) {
            return "redirect:/admin/promotions/list?error=cannot_delete_active";
        }
        try {
            promotionService.deletePromotion(promotionId);
        } catch (Exception e) {
            logger.warning("Error deleting promotion: " + e.getMessage());
            return "redirect:/admin/promotions/list?error=delete_failed";
        }
        return "redirect:/admin/promotions/list";
    }

    @RequestMapping("/admin/promotions/list")
    public String listPromotions(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @RequestParam(required = false) String error) {
        Page<PromotionDTO> promotionPage = promotionService.getAllPromotions(page, size);
        model.addAttribute("promotions", promotionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotionPage.getTotalPages());
        model.addAttribute("totalItems", promotionPage.getTotalElements());
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin/promotion/list";
    }

    @GetMapping("/admin/promotion/{id}")
    public String getPromotionDetailForAdmin(@PathVariable("id") Integer promotionId, Model model) {
        PromotionDTO promotion = promotionService.getPromotionById(promotionId);
        if (promotion == null) {
            return "redirect:/news?error=promotion_not_found";
        }
        model.addAttribute("promotion", promotion);
        model.addAttribute("ticketTypes", ticketTypeService.getTicketTypesMap());
        return "admin/promotion/newsDetail";
    }

    @GetMapping("/promotion/{id}")
    public String getPromotionDetail(@PathVariable("id") Integer promotionId, Model model) {
        PromotionDTO promotion = promotionService.getPromotionById(promotionId);
        if (promotion == null) {
            return "redirect:/news?error=promotion_not_found";
        }
        model.addAttribute("promotion", promotion);
        return "newsDetail";
    }
}