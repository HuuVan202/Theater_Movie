package movie_theater_gr4.project_gr4.repository;

import movie_theater_gr4.project_gr4.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

}