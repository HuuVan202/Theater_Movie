package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.MovieType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieTypeRepository extends JpaRepository<MovieType, Long> {
}
