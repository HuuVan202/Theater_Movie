package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.CinemaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Long> {
    @Query(value = "SELECT s.room_id, COUNT(s.seat_id) " +
            "FROM seat s " +
            "WHERE (s.seat_type_id = 1 OR s.seat_type_id = 2 OR s.seat_type_id = 3) AND s.is_active = true " +
            "GROUP BY s.room_id",
            nativeQuery = true)
    List<Object[]> findActiveSeatCountsByRoom();
}
