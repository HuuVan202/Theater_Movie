package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "movie_promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoviePromotion {
    @Id
    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "promotion_id")
    private Integer promotionId;
}