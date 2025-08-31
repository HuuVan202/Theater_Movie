package movie_theater_gr4.project_gr4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import movie_theater_gr4.project_gr4.model.MovieAgeRating;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
    private Long movieId;
    private String movieName;
    private String movieNameVn;
    private String movieNameEn;
    private String director;
    private String actor;
    private String content;
    private Integer duration;
    private String productionCompany;


    @JsonFormat(pattern = "yyyy-MM-dd")
    private List<LocalDate> showDates;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate fromDate;
    private LocalDate toDate;
    private String largeImageUrl;
    private String smallImageUrl;
    private String trailerUrl;
    private List<String> genres;
    private List<VersionDTO> versions;
    List<ShowTimeDTO> showtimes;
    //    private String ageRatingCode; // để lưu vào CSDL
    private MovieAgeRating ageRating;     // để hiển thị (ratingName)

    public String rating() {
        return ageRating.getRatingCode() + " - " + ageRating.getDescription();
    }

    public String getRatingCode() {
        return ageRating.getRatingCode() + " - " + ageRating.getRatingName();
    }


}

