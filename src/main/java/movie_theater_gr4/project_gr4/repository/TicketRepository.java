package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT COUNT(t) > 0 FROM Ticket t JOIN t.scheduleSeat ss WHERE ss.showtime.id = :showtimeId")
    boolean existsByShowtimeId(Long showtimeId);
}