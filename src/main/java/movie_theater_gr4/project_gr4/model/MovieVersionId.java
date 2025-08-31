package movie_theater_gr4.project_gr4.model;

import java.io.Serializable;
import java.util.Objects;

public class MovieVersionId implements Serializable {
    private Long movieId;
    private Long versionId;

    public MovieVersionId() {}

    public MovieVersionId(Long movieId, Long versionId) {
        this.movieId = movieId;
        this.versionId = versionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieVersionId that = (MovieVersionId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(versionId, that.versionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, versionId);
    }
}
