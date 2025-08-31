package movie_theater_gr4.project_gr4.model;

import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieOfPromotionId implements Serializable {

    private Movie movie;
    private Promotion promotion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieOfPromotionId)) return false;
        MovieOfPromotionId that = (MovieOfPromotionId) o;
        return Objects.equals(movie, that.movie) &&
                Objects.equals(promotion, that.promotion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, promotion);
    }
}
