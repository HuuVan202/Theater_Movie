package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {
    Optional<SeatType> findByTypeName(String typeName);
}
