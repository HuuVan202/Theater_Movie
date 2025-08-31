package movie_theater_gr4.project_gr4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieSearchDTO {
    private Long movieId;
    private String movieName;
    private String movieNameVn;
    private String movieNameEn;
    private String director;
    private String actor;
    private String content;
    private Integer duration;
    private String productionCompany;
    private MovieAgeRating ageRating;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String largeImageUrl;
    private String smallImageUrl;
    private String trailerUrl;
    private Integer featured;
    private List<String> genres;
    private Map<String, List<String>> highlights;
}
