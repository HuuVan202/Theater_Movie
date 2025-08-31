package movie_theater_gr4.project_gr4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTOCRUD {
    private Long movieId;

    @NotBlank(message = "Movie title is required")
    @Size(max = 100, message = "Movie title must be 100 characters or less")
    private String movieName;

    @Size(max = 100, message = "Vietnamese title must be 100 characters or less")
    private String movieNameVn;

    @Size(max = 100, message = "English title must be 100 characters or less")
    private String movieNameEn;

    @Size(max = 100, message = "Director name must be 100 characters or less")
    private String director;

    @Size(max = 200, message = "Actors field must be 200 characters or less")
    private String actor;

    @Size(max = 500, message = "Content must be 500 characters or less")
    private String content;


    @Min(value = 1, message = "Running time must be at least 1 minute")
    @Max(value = 999, message = "Running time must be 999 minutes or less")
    private Integer duration;

    @Size(max = 100, message = "Production company must be 100 characters or less")
    private String productionCompany;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> showDates;

    @NotNull(message = "Release date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private String largeImageUrl;

    private String smallImageUrl;

    private String trailerUrl;

    private Integer featured;
    @NotEmpty(message = "At least one genre must be selected")
    private List<Long> genres;

    @NotEmpty(message = "At least one version must be selected")
    private List<Long> versions;

    @NotBlank(message = "Age rating is required")
    private String ageRating;
}
