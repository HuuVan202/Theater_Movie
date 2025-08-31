package movie_theater_gr4.project_gr4.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import movie_theater_gr4.project_gr4.dto.MovieDTO;
import movie_theater_gr4.project_gr4.dto.MovieDTOCRUD;
import movie_theater_gr4.project_gr4.dto.MovieSearchDTO;
import movie_theater_gr4.project_gr4.model.Movie;
import movie_theater_gr4.project_gr4.model.MovieDocument;
import movie_theater_gr4.project_gr4.model.Type;
import movie_theater_gr4.project_gr4.repository.MovieElasticSearchRepository;
import movie_theater_gr4.project_gr4.service.*;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;
    private final TypeService typeService;
    private final MovieAgeRatingService movieAgeRatingService;
    private final VersionService versionService;
    private static final Logger logger = Logger.getLogger(MovieController.class.getName());
    private final Cloudinary cloudinary;
    private final MovieDocumentService movieDocumentService;


    // Read: List all movies with search functionality
    @GetMapping({"/listMovie", "/movieManagement"})
    public String listMovies(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) List<String> genres,
                             RedirectAttributes redirectAttributes) {
        try {
            Page<MovieSearchDTO> moviePage;
            PageRequest pageRequest = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "movieId"));

            List<String> normalizedGenres = genres != null ? genres.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()) : null;
            // Kiểm tra xem có keyword hoặc genres không
            if (StringUtils.hasText(keyword) || (genres != null && !genres.isEmpty())) {
                moviePage = movieDocumentService.searchMovieDocument(keyword, genres, pageRequest);
                if (moviePage.isEmpty()) {
                    String message = "Không tìm thấy phim nào";
                    if (StringUtils.hasText(keyword)) {
                        message += " khớp với từ khóa: " + keyword;
                    }
                    if (genres != null && !genres.isEmpty()) {
                        message += " với thể loại: " + String.join(", ", genres);
                    }
                    model.addAttribute("warningMessage", message);
                }
            } else {
                // Nếu không có keyword và genres, trả về toàn bộ phim
                moviePage = movieService.getAllMoviesWithSortA(pageRequest);
            }

            // Lấy danh sách tất cả thể loại
            List<Type> types = typeService.getAllTypes();
            model.addAttribute("allGenres", types);
            model.addAttribute("movies", moviePage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", moviePage.getTotalPages());
            model.addAttribute("keyword", keyword);
//            model.addAttribute("genres", genres != null ? genres : new ArrayList<>());

            return "admin/movie/movieManagement";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải danh sách phim: " + e.getMessage());
            return "redirect:/admin/movieManagement";
        }
    }

    @GetMapping("/detailMovie/{id}")
    public String viewMovieDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MovieDTO movie = movieService.getMovieDetail(id);
            model.addAttribute("versions", versionService.getVerSionByMovieId(id));
            model.addAttribute("ageRatings", movieAgeRatingService.getMovieAgeRatingByMovieId(id));
            model.addAttribute("types", typeService.getAllTypesByMovieId(id));
            model.addAttribute("movie", movie);
            return "admin/movie/viewMovieDetail_AM";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xem chi tiết phim: " + e.getMessage());
            return "redirect:/admin/movieManagement";
        }
    }

    // Create: Show create form
    @GetMapping("/createMovie")
    public String showCreateForm(Model model) {
        model.addAttribute("movie", new MovieDTOCRUD());
        model.addAttribute("versions", versionService.getAllTypes());
        model.addAttribute("ageRatings", movieAgeRatingService.geMovieAgeRatings());
        model.addAttribute("types", typeService.getAllTypes());
        return "admin/movie/createNewMovie";
    }

    @PostMapping("/createMovie")
    public String createMovie(@Valid @ModelAttribute("movie") MovieDTOCRUD movieDTOCRUD,
                              BindingResult bindingResult,
                              @RequestParam(value = "smallImageFile", required = false) MultipartFile smallImageFile,
                              @RequestParam(value = "largeImageFile", required = false) MultipartFile largeImageFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {

//        if (movieDTOCRUD.getFeatured() == null || movieDTOCRUD.getFeatured() < 1 || movieDTOCRUD.getFeatured() > 5) {
//            bindingResult.rejectValue("featured", "invalid.featured", "Vui lòng chọn mức độ ưu tiên hợp lệ (1-5).");
//        }

        if (movieDTOCRUD.getMovieName() != null && !movieDTOCRUD.getMovieName().trim().isEmpty()) {
            if (movieService.existsByMovieName(movieDTOCRUD.getMovieName().trim())) {
                bindingResult.rejectValue("movieName", "duplicate.movieName", "Tên phim đã tồn tại");
            }
        }
        if (movieDTOCRUD.getMovieNameEn() != null && !movieDTOCRUD.getMovieNameEn().trim().isEmpty()) {
            if (movieService.existsByMovieNameEn(movieDTOCRUD.getMovieNameEn().trim())) {
                bindingResult.rejectValue("movieNameEn", "duplicate.movieName", "Tên phim đã tồn tại");
            }
        }
        if (movieDTOCRUD.getMovieNameVn() != null && !movieDTOCRUD.getMovieNameVn().trim().isEmpty()) {
            if (movieService.existsByMovieNameVn(movieDTOCRUD.getMovieNameVn().trim())) {
                bindingResult.rejectValue("movieNameVn", "duplicate.movieName", "Tên phim đã tồn tại");
            }
        }

        // Validate genres
        if (movieDTOCRUD.getGenres() != null) {
            for (Long typeId : movieDTOCRUD.getGenres()) {
                if (!typeService.isValidTypeId(typeId)) {
                    bindingResult.rejectValue("genres", "invalid.genres", "Thể loại không hợp lệ");
                    break;
                }
            }
        }

        // Validate versions
        if (movieDTOCRUD.getVersions() != null) {
            for (Long versionId : movieDTOCRUD.getVersions()) {
                if (!versionService.isValidVersionId(versionId)) {
                    bindingResult.rejectValue("versions", "invalid.versions", "Phiên bản không hợp lệ");
                    break;
                }
            }
        }

        // Validate date
        if (movieDTOCRUD.getToDate() != null && movieDTOCRUD.getFromDate() != null &&
                movieDTOCRUD.getToDate().isBefore(movieDTOCRUD.getFromDate())) {
            bindingResult.rejectValue("toDate", "invalid.toDate", "Ngày kết thúc phải sau ngày phát hành");
        }

        // Validate smallImageFile
        if (smallImageFile != null && !smallImageFile.isEmpty()) {
            if (smallImageFile.getSize() > 2 * 1024 * 1024) {
                bindingResult.rejectValue("smallImageUrl", "invalid.smallImageUrl", "Kích thước ảnh nhỏ phải dưới 2MB");
            } else if (!smallImageFile.getContentType().startsWith("image/")) {
                bindingResult.rejectValue("smallImageUrl", "invalid.smallImageUrl", "Ảnh nhỏ phải là định dạng hình ảnh");
            }
        }

        // Validate largeImageFile
        if (largeImageFile != null && !largeImageFile.isEmpty()) {
            if (largeImageFile.getSize() > 2 * 1024 * 1024) {
                bindingResult.rejectValue("largeImageUrl", "invalid.largeImageUrl", "Kích thước ảnh lớn phải dưới 2MB");
            } else if (!largeImageFile.getContentType().startsWith("image/")) {
                bindingResult.rejectValue("largeImageUrl", "invalid.largeImageUrl", "Ảnh lớn phải là định dạng hình ảnh");
            }
        }

        // Nếu có lỗi thì trả lại form với thông báo tổng quát
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Vui lòng sửa các lỗi bên dưới");
            model.addAttribute("types", typeService.getAllTypes());
            model.addAttribute("ageRatings", movieAgeRatingService.geMovieAgeRatings());
            model.addAttribute("versions", versionService.getAllTypes());
            return "admin/movie/createNewMovie";
        }

        try {
            // Upload ảnh nhỏ
            if (smallImageFile != null && !smallImageFile.isEmpty()) {
                String fileName = "movies/" + UUID.randomUUID() + "_" + smallImageFile.getOriginalFilename();
                Map uploadResult = cloudinary.uploader().upload(smallImageFile.getBytes(),
                        ObjectUtils.asMap("public_id", fileName));
                movieDTOCRUD.setSmallImageUrl((String) uploadResult.get("secure_url"));
            }

            // Upload ảnh lớn
            if (largeImageFile != null && !largeImageFile.isEmpty()) {
                String fileName = "movies/" + UUID.randomUUID() + "_" + largeImageFile.getOriginalFilename();
                Map uploadResult = cloudinary.uploader().upload(largeImageFile.getBytes(),
                        ObjectUtils.asMap("public_id", fileName));
                movieDTOCRUD.setLargeImageUrl((String) uploadResult.get("secure_url"));
            }

            // Lưu phim
            Movie createdMovie = movieService.MovieDTOCRUDConvertToEntity(movieDTOCRUD);
            movieDocumentService.saveMovieDocument(createdMovie);

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phim thành công!");
            return "redirect:/admin/movieManagement";

        } catch (IOException e) {
            model.addAttribute("errorMessage", "Lỗi khi tải ảnh lên: " + e.getMessage());
            model.addAttribute("types", typeService.getAllTypes());
            model.addAttribute("ageRatings", movieAgeRatingService.geMovieAgeRatings());
            model.addAttribute("versions", versionService.getAllTypes());
            return "admin/movie/createNewMovie";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tạo phim: " + e.getMessage());
            return "redirect:/admin/movieManagement";
        }
    }

    @GetMapping("/editMovie/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            MovieDTOCRUD movie = movieService.getMovieDetailCRUD(id);
            model.addAttribute("priorityLevels", List.of(
                    new AbstractMap.SimpleEntry<>(1, "1 - Thấp nhất"),
                    new AbstractMap.SimpleEntry<>(2, "2"),
                    new AbstractMap.SimpleEntry<>(3, "3 - Trung bình"),
                    new AbstractMap.SimpleEntry<>(4, "4"),
                    new AbstractMap.SimpleEntry<>(5, "5 - Cao nhất"),
                    new AbstractMap.SimpleEntry<>(null, "Không ưu tiên")
            ));
            model.addAttribute("movie", movie);
            model.addAttribute("versions", versionService.getAllTypes());
            model.addAttribute("ageRatings", movieAgeRatingService.geMovieAgeRatings());
            model.addAttribute("types", typeService.getAllTypes());
            return "admin/movie/updateMovie_AM";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi tải thông tin phim: " + e.getMessage());
            return "redirect:/admin/movieManagement";
        }
    }

    @PostMapping("/editMovie/{id}")
    public String updateMovie(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("movie") MovieDTOCRUD movieDTO,
                              BindingResult result,
                              @RequestParam(value = "smallImageFile", required = false) MultipartFile smallImageFile,
                              @RequestParam(value = "largeImageFile", required = false) MultipartFile largeImageFile,
                              Model model,
                              RedirectAttributes redirectAttributes) {


        if (movieDTO.getMovieName() != null && !movieDTO.getMovieName().trim().isEmpty()) {
            if (movieService.existsByMovieNameAndNotId(movieDTO.getMovieName().trim(), id)) {
                result.rejectValue("movieName", "duplicate.movieName", "Tên phim đã tồn tại");
            }
        }

        // Kiểm tra trùng lặp movieNameVn, loại trừ phim hiện tại
        if (movieDTO.getMovieNameVn() != null && !movieDTO.getMovieNameVn().trim().isEmpty()) {
            if (movieService.existsByMovieNameVnAndNotId(movieDTO.getMovieNameVn().trim(), id)) {
                result.rejectValue("movieNameVn", "duplicate.movieNameVn", "Tên phim tiếng Việt đã tồn tại");
            }
        }

        // Kiểm tra trùng lặp movieNameEn, loại trừ phim hiện tại
        if (movieDTO.getMovieNameEn() != null && !movieDTO.getMovieNameEn().trim().isEmpty()) {
            if (movieService.existsByMovieNameEnAndNotId(movieDTO.getMovieNameEn().trim(), id)) {
                result.rejectValue("movieNameEn", "duplicate.movieNameEn", "Tên phim tiếng Anh đã tồn tại");
            }
        }
        // Custom validation for genres
        if (movieDTO.getGenres() != null) {
            movieDTO.getGenres().forEach(typeId -> {
                if (!typeService.isValidTypeId(typeId)) {
                    result.rejectValue("genres", "invalid.genres", "Thể loại không hợp lệ: " + typeId);
                }
            });
        }

        // Custom validation for versions
        if (movieDTO.getVersions() != null) {
            movieDTO.getVersions().forEach(versionId -> {
                if (!versionService.isValidVersionId(versionId)) {
                    result.rejectValue("versions", "invalid.versions", "Phiên bản không hợp lệ: " + versionId);
                }
            });
        }

        // Check if toDate is after fromDate
        if (movieDTO.getToDate() != null && movieDTO.getFromDate() != null && movieDTO.getToDate().isBefore(movieDTO.getFromDate())) {
            result.rejectValue("toDate", "invalid.toDate", "Ngày kết thúc phải sau ngày phát hành");
        }

        // Validate smallImageFile
        if (smallImageFile != null && !smallImageFile.isEmpty()) {
            if (smallImageFile.getSize() > 2 * 1024 * 1024) {
                result.rejectValue("smallImageUrl", "invalid.smallImageUrl", "Kích thước ảnh nhỏ phải dưới 2MB");
            } else if (!smallImageFile.getContentType().startsWith("image/")) {
                result.rejectValue("smallImageUrl", "invalid.smallImageUrl", "Ảnh nhỏ phải là định dạng hình ảnh");
            }
        }

        // Validate largeImageFile
        if (largeImageFile != null && !largeImageFile.isEmpty()) {
            if (largeImageFile.getSize() > 2 * 1024 * 1024) {
                result.rejectValue("largeImageUrl", "invalid.largeImageUrl", "Kích thước ảnh lớn phải dưới 2MB");
            } else if (!largeImageFile.getContentType().startsWith("image/")) {
                result.rejectValue("largeImageUrl", "invalid.largeImageUrl", "Ảnh lớn phải là định dạng hình ảnh");
            }
        }

        // Return to form if validation fails
        if (result.hasErrors()) {
            return populateModelAndReturnErrorView(id, movieDTO, model);
        }

        try {
            // Lấy bản ghi hiện tại của phim để giữ giá trị ảnh hiện tại
            MovieDTOCRUD originalMovie = movieService.getMovieDetailCRUD(id);

            // Handle small image upload
            if (smallImageFile != null && !smallImageFile.isEmpty()) {
                String smallImageUrl = uploadImage(smallImageFile, movieDTO.getSmallImageUrl());
                movieDTO.setSmallImageUrl(smallImageUrl);
            } else {
                // Giữ nguyên smallImageUrl hiện tại nếu không có ảnh mới
                movieDTO.setSmallImageUrl(originalMovie.getSmallImageUrl());
            }

            // Handle large image upload
            if (largeImageFile != null && !largeImageFile.isEmpty()) {
                String largeImageUrl = uploadImage(largeImageFile, movieDTO.getLargeImageUrl());
                movieDTO.setLargeImageUrl(largeImageUrl);
            } else {
                // Giữ nguyên largeImageUrl hiện tại nếu không có ảnh mới
                movieDTO.setLargeImageUrl(originalMovie.getLargeImageUrl());
            }

            // Update movie
            movieDTO.setMovieId(id);
            movieService.updateMovie(id, movieDTO);


            // Cập nhật movie và lấy đối tượng đã cập nhật
            Movie updatedMovie = movieService.updateMovie(id, movieDTO);
            // Lưu vào Elasticsearch
            movieDocumentService.saveMovieDocument(updatedMovie);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật phim thành công!");
            return "redirect:/admin/movieManagement";
        } catch (IOException e) {
            logger.warning("Error uploading image: " + e.getMessage());
            return populateModelAndReturnErrorView(id, movieDTO, model, "Lỗi khi tải ảnh lên: " + e.getMessage());
        } catch (Exception e) {
            logger.warning("Error updating movie: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi cập nhật phim: " + e.getMessage());
            return "redirect:/admin/movieManagement";
        }
    }

    /**
     * Uploads an image to Cloudinary and deletes the old image if it exists.
     */
    private String uploadImage(MultipartFile file, String oldImageUrl) throws IOException {
        String fileName = "movies/" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // Delete old image from Cloudinary
        if (file != null && !file.isEmpty()) {
            String publicId = extractPublicId(oldImageUrl);
            if (publicId != null) {
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    logger.info("Deleted old image from Cloudinary: "+ publicId);
                } catch (Exception ex) {
                    logger.warning("Failed to delete old image from Cloudinary:"+  publicId+"-"+ ex.getMessage());
                }
            }
        }

        // Upload new image
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("public_id", fileName));
        String imageUrl = (String) uploadResult.get("secure_url");
        logger.info("Uploaded new image to Cloudinary: "+ imageUrl);
        return imageUrl;
    }

    /**
     * Extracts publicId from a Cloudinary URL.
     */
    private String extractPublicId(String imageUrl) {
        String prefix = "https://res.cloudinary.com/dycfyoh8r/image/upload/";
        if (imageUrl != null && imageUrl.startsWith(prefix)) {
            return imageUrl.replace(prefix, "").replaceAll("\\.[a-zA-Z]+$", "");
        }
        return null;
    }

    /**
     * Populates the model with necessary attributes and returns the error view.
     */
    private String populateModelAndReturnErrorView(Long id, MovieDTOCRUD movieDTO, Model model, String... errorMessage) {
        MovieDTOCRUD originalMovie = movieService.getMovieDetailCRUD(id);
        if (movieDTO.getSmallImageUrl() == null || movieDTO.getSmallImageUrl().isEmpty()) {
            movieDTO.setSmallImageUrl(originalMovie.getSmallImageUrl());
        }
        if (movieDTO.getLargeImageUrl() == null || movieDTO.getLargeImageUrl().isEmpty()) {
            movieDTO.setLargeImageUrl(originalMovie.getLargeImageUrl());
        }
        model.addAttribute("errorMessage", errorMessage.length > 0 ? errorMessage[0] : "Vui lòng sửa các lỗi sau trước khi cập nhật phim");
        model.addAttribute("movie", movieDTO);
        model.addAttribute("versions", versionService.getAllTypes());
        model.addAttribute("ageRatings", movieAgeRatingService.geMovieAgeRatings());
        model.addAttribute("types", typeService.getAllTypes());
        return "admin/movie/updateMovie_AM";
    }

    @PostMapping("/deleteMovie/{id}")
    public String deleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            MovieDTOCRUD movie = movieService.getMovieDetailCRUD(id);
            if (movie == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phim không tồn tại!");
                return "redirect:/admin/movieManagement";
            }
            // Delete associated image files
            String smallImageUrl = movie.getSmallImageUrl();
            if (movie.getSmallImageUrl() != null && !movie.getSmallImageUrl().isEmpty()) {
                String publicId = smallImageUrl.replace("https://res.cloudinary.com/dycfyoh8r/image/upload/", "").replaceAll("\\.[a-zA-Z]+$", "");
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    logger.info("Deleted image from Cloudinary: " + publicId);
                } catch (Exception e) {
                    logger.warning("Failed to delete image from Cloudinary: " + publicId + " - " + e.getMessage());
                }
            }
            String largeImageUrl = movie.getLargeImageUrl();
            if (movie.getLargeImageUrl() != null && !movie.getLargeImageUrl().isEmpty()) {
                String publicId = largeImageUrl.replace("https://res.cloudinary.com/dycfyoh8r/image/upload/", "").replaceAll("\\.[a-zA-Z]+$", "");
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    logger.info("Deleted image from Cloudinary: " + publicId);
                } catch (Exception e) {
                    logger.warning("Failed to delete image from Cloudinary: " + publicId + " - " + e.getMessage());
                }
            }
            movieDocumentService.deleteMovieDocument(id);
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa phim thành công!");
            return "redirect:/admin/movieManagement";
        } catch ( Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa phim: " + e.getMessage());
            return "redirect:/admin/movieManagement";
        }
    }
    @GetMapping("/syncMovies")
    public String syncMoviesToElasticsearch(RedirectAttributes redirectAttributes) {
        new Thread(() -> {
            try {
                movieDocumentService.syncMoviesToElasticsearch();
                redirectAttributes.addFlashAttribute("successMessage", "Đồng bộ phim sang Elasticsearch thành công!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi đồng bộ phim: " + e.getMessage());
            }
        }).start();
        redirectAttributes.addFlashAttribute("successMessage", "Bắt đầu đồng bộ phim sang Elasticsearch...");
        return "redirect:/admin/movieManagement";
    }
    @GetMapping("/suggestMovies")
    @ResponseBody
    public ResponseEntity<List<String>> suggestMovies(@RequestParam("keyword") String keyword) {
        List<String> suggestions = movieDocumentService.suggestMovies(keyword);
        return ResponseEntity.ok(suggestions);
    }

}