package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Version;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VersionRepository extends JpaRepository<Version, Long> {
    List<Version> findByMovieVersions_Movie_MovieId(Long movieId);
}
