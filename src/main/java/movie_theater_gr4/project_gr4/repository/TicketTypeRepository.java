package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {
}