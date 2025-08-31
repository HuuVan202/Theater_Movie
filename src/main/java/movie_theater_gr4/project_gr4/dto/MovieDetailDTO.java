package movie_theater_gr4.project_gr4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDetailDTO {
    private Long movieId;
    private String movieName;
    private String movieNameVn;
    private String movieNameEn;
    private String director;
    private String actor;
    private String content;
    private Integer duration;
    private String productionCompany;
    private String ratingCode;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;
    private String largeImageUrl;
    private String smallImageUrl;
    private String trailerUrl;
    private List<VersionDetailDTO> versions;
    private String ageRating;
    private List<String> genres;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> showDates;

}
