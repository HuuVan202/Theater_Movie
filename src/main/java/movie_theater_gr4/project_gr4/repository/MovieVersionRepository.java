package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.MovieVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieVersionRepository extends JpaRepository<MovieVersion, Long> {
    boolean existsByMovieMovieIdAndVersionVersionId(Long movieId, Long versionId);
}