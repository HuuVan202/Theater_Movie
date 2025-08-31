package movie_theater_gr4.project_gr4.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.dto.PromotionDTO;
import movie_theater_gr4.project_gr4.model.MoviePromotion;
import movie_theater_gr4.project_gr4.model.Promotion;
import movie_theater_gr4.project_gr4.repository.MoviePromotionRepository;
import movie_theater_gr4.project_gr4.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final MoviePromotionRepository moviePromotionRepository;
    private final Cloudinary cloudinary;
    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);

    public Page<PromotionDTO> getAllPromotions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("promotionId").descending());
        Page<Promotion> promotions = promotionRepository.findAll(pageable);
        return promotions.map(this::convertToDTO);
    }

    public List<PromotionDTO> getAllActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findByStartTimeLessThanEqualAndEndTimeGreaterThanEqualAndIsActiveTrue(now);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getActivePromotionsByDayOfWeek(Integer dayOfWeek) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotionsByDayOfWeek(dayOfWeek, now);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getActivePromotionsByTicketType(Integer ticketTypeId) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotionsByTicketType(ticketTypeId, now);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PromotionDTO convertToDTO(Promotion promotion) {
        return PromotionDTO.builder()
                .promotionId(promotion.getPromotionId())
                .title(promotion.getTitle())
                .detail(promotion.getDetail())
                .discountLevel(promotion.getDiscountLevel())
                .discountAmount(promotion.getDiscountAmount())
                .minTickets(promotion.getMinTickets())
                .maxTickets(promotion.getMaxTickets())
                .dayOfWeek(promotion.getDayOfWeek())
                .ticketTypeId(promotion.getTicketTypeId())
                .startTime(promotion.getStartTime())
                .endTime(promotion.getEndTime())
                .imageUrl(promotion.getImageUrl())
                .active(promotion.isActive())
                .maxUsage(promotion.getMaxUsage())
                .build();
    }

    @Transactional
    public void addPromotion(PromotionDTO promotionDTO, MultipartFile imageFile) {
        // Xử lý ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageFile.getSize() > 2 * 1024 * 1024) {
                throw new IllegalArgumentException("Kích thước file vượt quá 2MB.");
            }
            if (!imageFile.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File không phải là ảnh.");
            }
            try {
                String fileName = "promotions/" + UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(),
                        ObjectUtils.asMap("public_id", fileName));
                String imageUrl = (String) uploadResult.get("secure_url");
                promotionDTO.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi tải ảnh lên Cloudinary: " + e.getMessage());
            }
        }

        // Lưu khuyến mãi
        Promotion promotion = new Promotion();
        promotion.setTitle(promotionDTO.getTitle());
        promotion.setDetail(promotionDTO.getDetail());
        promotion.setDiscountLevel(promotionDTO.getDiscountLevel());
        promotion.setDiscountAmount(promotionDTO.getDiscountAmount());
        promotion.setMinTickets(promotionDTO.getMinTickets());
        promotion.setMaxTickets(promotionDTO.getMaxTickets());
        promotion.setDayOfWeek(promotionDTO.getDayOfWeek());
        promotion.setTicketTypeId(promotionDTO.getTicketTypeId());
        promotion.setStartTime(promotionDTO.getStartTime());
        promotion.setEndTime(promotionDTO.getEndTime());
        promotion.setImageUrl(promotionDTO.getImageUrl());
        promotion.setActive(promotionDTO.isActive());
        promotion.setMaxUsage(promotionDTO.getMaxUsage());
        Promotion savedPromotion = promotionRepository.save(promotion);

        // Lưu liên kết với phim
        if (promotionDTO.getMovieId() != null) {
            MoviePromotion moviePromotion = new MoviePromotion();
            moviePromotion.setPromotionId(savedPromotion.getPromotionId()); // Sử dụng promotionId từ savedPromotion
            moviePromotion.setMovieId(promotionDTO.getMovieId());
            moviePromotionRepository.save(moviePromotion);
        }
    }

    @Transactional
    public void updatePromotion(PromotionDTO promotionDTO, MultipartFile imageFile) {
        Promotion promotion = promotionRepository.findById(promotionDTO.getPromotionId())
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promotionDTO.getPromotionId()));

        // Xử lý ảnh nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            if (imageFile.getSize() > 2 * 1024 * 1024) {
                throw new IllegalArgumentException("Kích thước file vượt quá 2MB.");
            }
            if (!imageFile.getContentType().startsWith("image/")) {
                throw new IllegalArgumentException("File không phải là ảnh.");
            }
            // Xóa ảnh cũ trên Cloudinary
            String oldImageUrl = promotionDTO.getImageUrl();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                String publicId = oldImageUrl.replace("https://res.cloudinary.com/dycfyoh8r/image/upload/", "").replaceAll("\\.[a-zA-Z]+$", "");
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    logger.info("Deleted old image from Cloudinary: " + publicId);
                } catch (Exception ex) {
                    logger.warn("Failed to delete old image from Cloudinary: " + publicId + " - " + ex.getMessage());
                }
            }
            // Tải ảnh mới lên Cloudinary
            try {
                String fileName = "promotions/" + UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(),
                        ObjectUtils.asMap("public_id", fileName));
                String imageUrl = (String) uploadResult.get("secure_url");
                promotionDTO.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi tải ảnh lên Cloudinary: " + e.getMessage());
            }
        }

        // Cập nhật thông tin khuyến mãi
        promotion.setTitle(promotionDTO.getTitle());
        promotion.setDetail(promotionDTO.getDetail());
        promotion.setDiscountLevel(promotionDTO.getDiscountLevel());
        promotion.setDiscountAmount(promotionDTO.getDiscountAmount());
        promotion.setMinTickets(promotionDTO.getMinTickets());
        promotion.setMaxTickets(promotionDTO.getMaxTickets());
        promotion.setDayOfWeek(promotionDTO.getDayOfWeek());
        promotion.setTicketTypeId(promotionDTO.getTicketTypeId());
        promotion.setStartTime(promotionDTO.getStartTime());
        promotion.setEndTime(promotionDTO.getEndTime());
        promotion.setImageUrl(promotionDTO.getImageUrl());
        promotion.setActive(promotionDTO.isActive());
        promotion.setMaxUsage(promotionDTO.getMaxUsage());
        promotionRepository.save(promotion);

        // Xử lý liên kết với phim
        moviePromotionRepository.deleteByPromotionId(promotionDTO.getPromotionId());
        if (promotionDTO.getMovieId() != null) {
            MoviePromotion moviePromotion = new MoviePromotion();
            moviePromotion.setPromotionId(promotionDTO.getPromotionId());
            moviePromotion.setMovieId(promotionDTO.getMovieId());
            moviePromotionRepository.save(moviePromotion);
        }
    }

    @Transactional
    public void deletePromotion(Integer promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promotionId));

        // Xóa ảnh trên Cloudinary
        String imageUrl = promotion.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String publicId = imageUrl.replace("https://res.cloudinary.com/dycfyoh8r/image/upload/", "").replaceAll("\\.[a-zA-Z]+$", "");
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                logger.info("Deleted image from Cloudinary: " + publicId);
            } catch (Exception e) {
                logger.warn("Failed to delete image from Cloudinary: " + publicId + " - " + e.getMessage());
            }
        }

        // Xóa bản ghi trong movie_promotion
        moviePromotionRepository.deleteByPromotionId(promotionId);

        // Xóa bản ghi trong promotion
        promotionRepository.deleteById(promotionId);
    }

    public PromotionDTO getPromotionById(Integer promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + promotionId));
        PromotionDTO promotionDTO = convertToDTO(promotion);
        // Lấy movieId từ movie_promotion
        MoviePromotion moviePromotion = moviePromotionRepository.findByPromotionId(promotionId);
        if (moviePromotion != null) {
            promotionDTO.setMovieId(moviePromotion.getMovieId());
        }
        return promotionDTO;
    }

    public List<PromotionDTO> getLatestPromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findLatestPromotions(now);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getActivePromotionsByMovie(Long movieId) {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotionsByMovie(movieId, now);
        return promotions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


}