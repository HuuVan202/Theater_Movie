package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "movie_age_rating")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieAgeRating {
    @Id
    @Column(name = "rating_code")
    private String ratingCode;

    @Column(name = "rating_name")
    private String ratingName;

    @Column(name = "description")
    private String description;
}
