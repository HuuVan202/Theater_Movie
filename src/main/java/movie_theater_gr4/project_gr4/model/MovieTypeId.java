package movie_theater_gr4.project_gr4.model;

import java.io.Serializable;
import java.util.Objects;

public class MovieTypeId implements Serializable {
    private Long movieId;
    private Long typeId;

    public MovieTypeId() {}

    public MovieTypeId(Long movieId, Long typeId) {
        this.movieId = movieId;
        this.typeId = typeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieTypeId that = (MovieTypeId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(typeId, that.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, typeId);
    }
}
