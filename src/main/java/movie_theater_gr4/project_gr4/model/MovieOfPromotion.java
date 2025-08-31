package movie_theater_gr4.project_gr4.model;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;

@Entity
@Table(name = "movie_promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MovieOfPromotionId.class) // Composite key sẽ được định nghĩa sau
public class MovieOfPromotion {

    @Id
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Id
    @ManyToOne
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;


}

