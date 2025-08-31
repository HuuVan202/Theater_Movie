package movie_theater_gr4.project_gr4.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectMovieDTO {
    private Long movieId;
    private String movieName;
    private String movieNameEn;
    private String movieNameVn;
    private String director;
    private String actor;
    private String content;
    private Integer duration;
    private String ratingCode;
    private String productionCompany;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String trailerUrl;
    private String smallImageUrl;
    private String largeImageUrl;
    private MovieAgeRating movieAgeRating;

    private List<VersionDTO> versions;      // Một phim có nhiều version
    private List<ShowtimeDTO> showtimes;    // Một phim có nhiều suất chiếu

}
